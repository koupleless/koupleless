package com.alipay.sofa.web.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class BaseApplication {
	private static Logger LOGGER = LoggerFactory.getLogger(BaseApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(BaseApplication.class, args);
		LOGGER.info("BaseApplication start!");
		LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
		LOGGER.info("BaseApplication classLoader: " + BaseApplication.class.getClassLoader());
	}
}
