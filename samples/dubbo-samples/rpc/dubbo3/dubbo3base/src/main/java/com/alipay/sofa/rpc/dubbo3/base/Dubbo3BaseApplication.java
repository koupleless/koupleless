package com.alipay.sofa.rpc.dubbo3.base;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class Dubbo3BaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dubbo3BaseApplication.class, args);
    }

}
