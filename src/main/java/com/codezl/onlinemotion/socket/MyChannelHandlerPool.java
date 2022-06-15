package com.codezl.onlinemotion.socket;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/14/14:10
 * @Description:
 */
public class MyChannelHandlerPool extends ChannelHandlerAdapter {
    public static ConcurrentMap<String, ChannelHandlerContext> onlineMap = new ConcurrentHashMap<>();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        onlineMap.put(ctx.channel().id().asLongText(),ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    public static void add(String id, ChannelHandlerContext ctx) {
        onlineMap.put(id,ctx);
    }

}
