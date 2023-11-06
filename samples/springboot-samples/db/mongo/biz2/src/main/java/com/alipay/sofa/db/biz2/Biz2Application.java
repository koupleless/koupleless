package com.alipay.sofa.db.biz2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.alipay.sofa.db.biz2.infra.db")
public class Biz2Application {
    private static Logger LOGGER = LoggerFactory.getLogger(Biz2Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Biz2Application.class, args);

        LOGGER.info("BaseApplication start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("BaseApplication classLoader: " + Biz2Application.class.getClassLoader());
    }

}
