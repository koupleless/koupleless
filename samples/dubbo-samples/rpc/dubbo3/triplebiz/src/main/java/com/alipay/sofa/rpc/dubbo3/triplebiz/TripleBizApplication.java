package com.alipay.sofa.rpc.dubbo3.triplebiz;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo(scanBasePackages=" com.alipay.sofa.rpc.dubbo3.triplebiz.service")
public class TripleBizApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(TripleBizApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TripleBizApplication.class, args);

        LOGGER.info("TripleBizApplication start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("TripleBizApplication classLoader: " + TripleBizApplication.class.getClassLoader());
    }

}
