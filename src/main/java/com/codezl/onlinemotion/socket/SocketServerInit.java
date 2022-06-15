package com.codezl.onlinemotion.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/14/18:16
 * @Description:
 */
@Service
public class SocketServerInit implements ApplicationRunner {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    SocketConfig config;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.print("\n启动netty\n");
        start();
    }

    public void start() {
        WebSocketServer socket = config.socket();
        logger.info("netty开始启动");
        //启动netty
        //WebSocketServer webSocketServer = (WebSocketServer) context.getBean("webSocketServer");
        int port = 28888;
        try {
            socket.run(port);
        } catch (Exception e) {
            logger.error("netty启动异常" + e);
        }
    }
}
