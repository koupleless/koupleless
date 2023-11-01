package com.alipay.sofa.base;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource({ "classpath*:META-INF/spring/service.xml"})
@SpringBootApplication
@MapperScan(basePackages = "com.alipay.sofa.base.mapper")
public class BaseApplication {
	private static Logger LOGGER = LoggerFactory.getLogger(BaseApplication.class);

	public static void main(String[] args) {
		// 设置内嵌方式启动多模块运行容器, 也可以放在启动参数中
		System.setProperty("sofa.ark.embed.enable", "true");
		System.setProperty("sofa.ark.plugin.export.class.enable", "true");

		SpringApplication.run(BaseApplication.class, args);
		LOGGER.info("BaseApplication start!");
		LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
		LOGGER.info("BaseApplication classLoader: " + BaseApplication.class.getClassLoader());
	}
}
