package com.alipay.sofa.biz2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

@SpringBootApplication(exclude = {JacksonAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class Biz2Application {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Biz2Application.class).web(WebApplicationType.SERVLET);

		// set biz to use resource loader.
		ResourceLoader resourceLoader = new DefaultResourceLoader(Biz2Application.class.getClassLoader());
		builder.resourceLoader(resourceLoader);

		ConfigurableApplicationContext context = builder.build().run(args);
		System.out.println("Biz start!");
		System.out.println("Biz spring boot version: " + SpringApplication.class.getPackage().getImplementationVersion());
		System.out.println("Biz classLoader: " + Biz2Application.class.getClassLoader());

	}
}
