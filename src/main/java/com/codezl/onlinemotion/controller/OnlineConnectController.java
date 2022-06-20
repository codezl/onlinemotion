package com.codezl.onlinemotion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/20/11:33
 * @Description:
 */
@RestController
@RequestMapping("connect")
public class OnlineConnectController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @RequestMapping("connect2one")
    public void connect2one(String toUser) {
        String localUser = "19";
        redisTemplate.opsForValue().set("connect2one"+localUser,toUser);
    }
}
