package com.alipay.sofa.logging.biz2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Biz2Application {

    private static Logger LOGGER = LoggerFactory.getLogger(Biz2Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Biz2Application.class, args);

        LOGGER.info("BaseApplication start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("BaseApplication classLoader: " + Biz2Application.class.getClassLoader());
    }

}
