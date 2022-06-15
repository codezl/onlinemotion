package com.codezl.onlinemotion.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Main {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        /**
         * 容器上下文对象
         */
        private static FileSystemXmlApplicationContext context;

        public static void main(String[] args) {
                Main main = new Main();
                main.start();
        }

        private void start() {
                //加载spring
                context = new FileSystemXmlApplicationContext("classpath:config/netty.xml");
                logger.info("netty开始启动");
                //启动netty
                WebSocketServer webSocketServer = (WebSocketServer) context.getBean("webSocketServer");
                int port = 8080;
                try {
                        webSocketServer.run(port);
                } catch (Exception e) {
                        logger.error("netty启动异常" + e);
                }
        }
}
