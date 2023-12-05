/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo26.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 *
 * @author syd
 * @version BizApplication.java, v 0.1 2023年10月31日 19:42 syd
 */

@SpringBootApplication
@ImportResource("classpath:dubbo.xml")
public class BizApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(BizApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);

        LOGGER.info("BizApplication start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("BizApplication classLoader: " + BizApplication.class.getClassLoader());
    }

}