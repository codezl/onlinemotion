package com.codezl.onlinemotion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.codezl")
@MapperScan("com.codezl.onlinemotion.mapper")
public class OnlinemotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlinemotionApplication.class, args);
    }

}
