package com.codezl.onlinemotion.socket;

import com.alibaba.fastjson.JSONObject;
import com.codezl.onlinemotion.pojo.dto.MsgTransferDto;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;

@Sharable
@Component
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private WebSocketServerHandshaker handshaker;
    // 存储好消息通道主体
    public static ConcurrentMap<String, ChannelHandlerContext> onlineWs = new ConcurrentHashMap<>();
    // 自定义属性 此处不可以,本类为处理类而不是ws实例
    private static ChannelHandlerContext context;
    // 使用ThreadLocal存储当前socket线程的用户信息，做到线程数据隔离，并且最后记得清除，避免内存泄露
    private static final ThreadLocal<String> THREAD_LOCAL_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> THREAD_LOCAL_RECEIVER = new ThreadLocal<>();

    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        //传统的HTTP接入
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } //WebSocket接入
        else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //判断是否关闭链路指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        //判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content()).retain());
            return;
        }
        //本例程仅支持文本信息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
        Channel channel = ctx.channel();
        InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
        String hostName = insocket.getHostName();
        String hostString = insocket.getHostString();
        int port = insocket.getPort();
        String clientIP = insocket.getAddress().getHostAddress();
        logger.info("客户端消息," + " clientIP:" + clientIP + " hostName:" + hostName + " hostString:" + hostString + " port:" + port);
        //接收到的消息，可进行后续操作
        String protocol = ((TextWebSocketFrame) frame).text();
        //消息返回
        ctx.channel().write(new TextWebSocketFrame(protocol + "欢迎使用Netty WebSocket服务，现在时刻:" + new Date().toString()));
        ChannelHandlerContext zs = onlineWs.get("/zs");
        if (zs == null) {
            return;
        }
        zs.channel().writeAndFlush(new TextWebSocketFrame("发给zs信息"));
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        //返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }
        //如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //如果HTTP解码失败，返回HTTP异常
        if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        //构造握手响应返回，本机测试
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket", null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            //把握手消息返回给客户端
            handshaker.handshake(ctx.channel(), req);
            //System.out.print("\n后续验证\n");
            // 自己加入注册验证
            String uri = req.uri();
            System.out.print("\n连接地址" + uri + "\n");
            if (uri != null && !"/".equals(uri)) {
                System.out.print("socket连接成功");
                MyChannelHandlerPool.add(uri, ctx);
                onlineWs.put(uri, ctx);
                THREAD_LOCAL_USER.set(uri);
                // 做一些数据库初始化操作，比如初始化联系人（可能为掉线重连）
                // 为了节省内存，也可以将建立连接的初始化数据，返回到客户端，保存在用户端
                THREAD_LOCAL_RECEIVER.set("/zs");
            } else {
                //消息返回
                ctx.channel().writeAndFlush(new TextWebSocketFrame("socket连接路径为空"));
                ctx.close();
                throw new RuntimeException("socket连接路径为空");
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        clearThreadCache();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.messageReceived(ctx, msg);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.error("连接关闭" + THREAD_LOCAL_USER.get());
        // 必须调用remove，防止内存泄露
        clearThreadCache();
    }

    //
    public static MsgTransferDto.serverMsg dlewithRecMsg(String msg,ChannelHandlerContext ctx) {
        MsgTransferDto.serverMsg dto = new MsgTransferDto.serverMsg();
        if (msg == null) {
            return dto;
        }
        try {
            MsgTransferDto.miniappMsg miniappMsg = JSONObject.parseObject(msg, MsgTransferDto.miniappMsg.class);

        } catch (Exception e) {
            log.error("消息格式错误,来自{}", THREAD_LOCAL_USER.get());
        }
        return dto;
    }

    /**
     * 清除缓存方法
     */
    public void clearThreadCache() {
        THREAD_LOCAL_USER.remove();
        THREAD_LOCAL_RECEIVER.remove();
    }

}