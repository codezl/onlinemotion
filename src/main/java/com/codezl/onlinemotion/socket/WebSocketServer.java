package com.codezl.onlinemotion.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ayue
 */
public class WebSocketServer {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        @Autowired
        private SockerChannelInitializer sockerChannelInitializer;
 
        private int bossGroupNum = 1;
        private int workerGroupNum = 2;
        private ChannelHandler handler = new LoggingHandler(LogLevel.INFO);
 
        public void run(int port) throws Exception {
                //创建两组线程，监听连接和工作
                EventLoopGroup bossGroup = new NioEventLoopGroup(bossGroupNum, new DefaultThreadFactory("Server-Boss"));
                EventLoopGroup workerGroup = new NioEventLoopGroup(workerGroupNum, new DefaultThreadFactory("Server-Worker"));
                try {
                        //Netty用于启动Nio服务端的启动类
                        ServerBootstrap b = new ServerBootstrap();
                        b.group(bossGroup, workerGroup);
                        //注册NioServerSocketChannel
                        b.channel(NioServerSocketChannel.class);
                        //注册进出站日志记录
                        b.handler(handler);
                        //注册处理器
                        b.childHandler(sockerChannelInitializer);
                        Channel ch = b.bind(port).sync().channel();
                        ch.closeFuture().sync();
                } finally {
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                }
        }
}