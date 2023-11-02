package com.alipay.sofa.biz;

import com.alipay.sofa.biz.rest.SampleController;
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
public class BizApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(BizApplication.class).web(WebApplicationType.SERVLET);

		// set biz to use resource loader.
		ResourceLoader resourceLoader = new DefaultResourceLoader(BizApplication.class.getClassLoader());
		builder.resourceLoader(resourceLoader);

		ConfigurableApplicationContext context = builder.build().run(args);

		SampleController controller = context.getBean(SampleController.class);
		controller.hello();

		System.out.println("Biz start!");
		System.out.println("Biz spring boot version: " + SpringApplication.class.getPackage().getImplementationVersion());
		System.out.println("Biz classLoader: " + BizApplication.class.getClassLoader());

	}
}
