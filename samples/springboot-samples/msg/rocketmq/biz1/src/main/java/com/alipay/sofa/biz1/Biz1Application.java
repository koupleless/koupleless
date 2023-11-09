package com.alipay.sofa.biz1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class Biz1Application {
	private static Logger LOGGER = LoggerFactory.getLogger(Biz1Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Biz1Application.class, args);
		LOGGER.info("Biz1Application start!");
		LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
		LOGGER.info("Biz1Application classLoader: " + Biz1Application.class.getClassLoader());
	}
}
