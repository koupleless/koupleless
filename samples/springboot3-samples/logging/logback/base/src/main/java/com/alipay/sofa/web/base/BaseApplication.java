package com.alipay.sofa.web.base;

import ch.qos.logback.classic.ClassicConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

@ImportResource({ "classpath*:META-INF/spring/service.xml"})
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class BaseApplication {

	static {
		// 建议加到jvm 参数中
		// 需要保证在 slf4j static bind 之前，（如，首次 getLogger、类加载 SpringApplication 之前）
		System.setProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR,
			"com.alipay.sofa.serverless.adapter.ArkLogbackContextSelector");
	}

	private static Logger LOGGER = LoggerFactory.getLogger(BaseApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(
				BaseApplication.class, args);
		context.getBean("sampleService");
		LOGGER.info("BaseApplication start!");
		LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
		LOGGER.info("BaseApplication classLoader: " + BaseApplication.class.getClassLoader());
	}
}
