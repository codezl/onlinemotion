package com.codezl.onlinemotion.socket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/14/18:28
 * @Description:
 */
@Configuration
@Component
public class SocketConfig {

    @Bean
    public WebSocketServer socket() {
        return new WebSocketServer();
    }

}
