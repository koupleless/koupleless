package com.alipay.sofa.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaseApplication.class, args);
		System.out.println("SofaArkSpringGuidesApplication start!");
		System.out.println("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
		System.out.println("SofaArkSpringGuidesApplication classLoader: " + BaseApplication.class.getClassLoader());
	}
}
