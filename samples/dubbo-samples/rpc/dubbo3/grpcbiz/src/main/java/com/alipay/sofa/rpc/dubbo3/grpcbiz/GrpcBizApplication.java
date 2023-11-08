package com.alipay.sofa.rpc.dubbo3.grpcbiz;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class GrpcBizApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(GrpcBizApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GrpcBizApplication.class, args);

        LOGGER.info("GrpcBizApplication start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("GrpcBizApplication classLoader: " + GrpcBizApplication.class.getClassLoader());
    }

}
