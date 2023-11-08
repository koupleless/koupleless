/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo26.biz2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 *
 * @author syd
 * @version Biz2Application.java, v 0.1 2023年10月31日 19:42 syd
 */

@SpringBootApplication
@ImportResource("classpath:provider.xml")
public class Biz2Application {
    private static Logger LOGGER = LoggerFactory.getLogger(Biz2Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Biz2Application.class, args);

        LOGGER.info("Biz2Application start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("Biz2Application classLoader: " + Biz2Application.class.getClassLoader());
    }

}