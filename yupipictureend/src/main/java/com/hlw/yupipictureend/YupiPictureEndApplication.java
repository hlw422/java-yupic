package com.hlw.yupipictureend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.hlw.yupipictureend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class YupiPictureEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(YupiPictureEndApplication.class, args);
    }

}
