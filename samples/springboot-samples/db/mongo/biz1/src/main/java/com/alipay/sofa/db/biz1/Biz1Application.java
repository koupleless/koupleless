package com.alipay.sofa.db.biz1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.alipay.sofa.db.biz1.infra.db")
public class Biz1Application {
    private static Logger LOGGER = LoggerFactory.getLogger(Biz1Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Biz1Application.class, args);

        LOGGER.info("BaseApplication start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("BaseApplication classLoader: " + Biz1Application.class.getClassLoader());
    }

}
