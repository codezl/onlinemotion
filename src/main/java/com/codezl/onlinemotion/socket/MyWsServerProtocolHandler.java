package com.codezl.onlinemotion.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/14/10:03
 * @Description:
 */
public class MyWsServerProtocolHandler extends WebSocketServerProtocolHandler {

    public MyWsServerProtocolHandler(String websocketPath) {
        super(websocketPath);
        System.out.print(this.getClass().getName()+"连接地址"+websocketPath);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.print("\n连接信息"+msg+"\n");
    }
}
