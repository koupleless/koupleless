package com.alipay.sofa.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

@ImportResource({ "classpath*:META-INF/spring/service.xml" })
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BaseApplication {

	public static final Logger logger = LoggerFactory.getLogger(BaseApplication.class);

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(BaseApplication.class, args);

		System.out.println("SofaArkSpringGuidesApplication start!");
		System.out.println("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
		System.out.println("SofaArkSpringGuidesApplication classLoader: " + BaseApplication.class.getClassLoader());

	}

}
