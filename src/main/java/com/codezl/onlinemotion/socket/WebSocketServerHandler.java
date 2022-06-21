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
import java.util.Map;
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
    // 除了记录reciever也可以使用聊天室的形式进行消息传递
    private static final ThreadLocal<String> THREAD_CHATROOM = new ThreadLocal<>();
    // 因为threadlocal只能使用本线程获取，所以使用map的形式给外部开放接口设置参数
    private static final ConcurrentMap<String,String> CONNECT_LINK = new ConcurrentHashMap<>();
    // 也可以尝试将本线程存起来
    private static final ConcurrentMap<String,Thread> USER_THREAD = new ConcurrentHashMap<>();
    // 记录位置信息
    private static final ConcurrentMap<String, String> USER_LOC = new ConcurrentHashMap<>();

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
        //ctx.channel().write(new TextWebSocketFrame(protocol + "欢迎使用Netty WebSocket服务，现在时刻:" + new Date().toString()));
        dlewithRecMsg(protocol,ctx);
        // 额外信息处理
        // ChannelHandlerContext zs = onlineWs.get("/zs");
        // if (zs == null) {
        //     return;
        // }
        // zs.channel().writeAndFlush(new TextWebSocketFrame("发给zs信息"));
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
                if (onlineWs.get(uri)!=null) {
                     alreadyOnline(ctx,uri);
                    // return;
                }
                MyChannelHandlerPool.add(uri, ctx);
                onlineWs.put(uri, ctx);
                THREAD_LOCAL_USER.set(uri);
                // 做一些数据库初始化操作，比如初始化联系人（可能为掉线重连）
                // 为了节省内存，也可以将建立连接的初始化数据，返回到客户端，保存在用户端
                // THREAD_LOCAL_RECEIVER.set("/zs");
                initData();
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
    public static MsgTransferDto.serverMsg dlewithRecMsg(String msg,ChannelHandlerContext nowCtx) {
        MsgTransferDto.serverMsg dto = new MsgTransferDto.serverMsg();
        if (msg == null || "".equals(msg)) {
            return dto;
        }
        try {
            MsgTransferDto.miniappMsg miniappMsg = JSONObject.parseObject(msg, MsgTransferDto.miniappMsg.class);
            Integer op = miniappMsg.getOp();
            if (op == 0) {
                sendMsg(nowCtx, dto, miniappMsg);
            }else if (op == 1) {
                // 建立连线
            }else {
                uploadLoc(nowCtx,miniappMsg.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("消息{}格式错误,来自{}", msg,THREAD_LOCAL_USER.get());
            dto.setMsgType(503);
            nowCtx.channel().write(new TextWebSocketFrame(JSONObject.toJSONString(dto)));
        }
        return dto;
    }

    /**
     * 发送信息的内部逻辑
     */
    public static void sendMsg(ChannelHandlerContext nowCtx,MsgTransferDto.serverMsg dto,MsgTransferDto.miniappMsg miniappMsg) {
        int msgType = miniappMsg.getMsgType();
        dto.setMsgType(msgType);
        if (msgType == 0) {
            dto.setMsg("收到系统信息:"+miniappMsg.getMsg());
            nowCtx.channel().write(new TextWebSocketFrame(JSONObject.toJSONString(dto)));
        }else if (msgType == 1) {
            String receiver = THREAD_LOCAL_RECEIVER.get();
            // 或者通过信息传递通信人
            // String recv = miniappMsg.getReceiverUser();
            ChannelHandlerContext recCtx = onlineWs.get(miniappMsg.getReceiver());
            if (recCtx == null) {
                dto.setMsg("用户不在线");
                dto.setMsgType(404);
                nowCtx.channel().write(new TextWebSocketFrame(JSONObject.toJSONString(dto)));
            }else {
                dto.setMsg(miniappMsg.getMsg());
                dto.setFromUser(THREAD_LOCAL_USER.get());
                recCtx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(dto)));
            }
        }else if (msgType == 2) {
            //  双人聊天
        }else {
            //
        }
    }

    /**
     * 上传位置消息
     * 只要在上传位置，就是在与好友共享位置
     */
    public static void uploadLoc(ChannelHandlerContext cxt,String loc) {
        USER_LOC.put(THREAD_LOCAL_USER.get(),loc);
        // 在连线的好友位置
        // String s = USER_LOC.get(THREAD_LOCAL_RECEIVER.get());
        String s = "23.073769,114.409414";
        MsgTransferDto.serverMsg dto = new MsgTransferDto.serverMsg();
        dto.setOp(3);
        dto.setMsg(s);
        cxt.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(dto)));
    }

    /**
     * 初始化连接数据
     */
    public void initData() {
        // Data...
    }

    /**
     * 已经在线提示
     * 或者清除已存在连接
     */
    public void alreadyOnline(ChannelHandlerContext cxt,String uri) {
        // 1.清除原来的连接
        ChannelHandlerContext old = onlineWs.get(uri);
        onlineWs.remove(uri);
        old.close();
        // 2.发送已连接/重复上线信息
        /**
            MsgTransferDto.serverMsg dto = new MsgTransferDto.serverMsg();
            dto.setMsgType(0);
            dto.setMsg("已在线");
            dto.setFromUser(THREAD_LOCAL_USER.get());
            cxt.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(dto)));
            cxt.close();
         */
    }

    /**
     * 清除缓存方法
     */
    public void clearThreadCache() {
        THREAD_LOCAL_USER.remove();
        THREAD_LOCAL_RECEIVER.remove();
    }

    /** 设置接收者
     * @param receiver
     */
    public static void setReceiver(String receiver) {
        THREAD_LOCAL_RECEIVER.set(receiver);
    }

    /**
     * 设置聊天室
     */
    public static void setChatroom(String chatroom) {
        THREAD_CHATROOM.set(chatroom);
    }

    /**
     * 设置线程
     */
    public static void setUserThread(String user) {
        USER_THREAD.put(user,Thread.currentThread());
    }

    /**
     * 获取线程
     */
    public ThreadLocal getUserThread(String user) {
        Thread t = USER_THREAD.get(user);
        // 无法获取thread中的threadlocals
        return null;
    }


}