package com.alipay.sofa.base;

import com.alipay.sofa.base.rest.SampleController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BaseApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(BaseApplication.class, args);
		SampleController controller = (SampleController) context.getBean("sampleController");

		controller.hello();

		System.out.println("SofaArkSpringGuidesApplication start!");
		System.out.println("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
		System.out.println("SofaArkSpringGuidesApplication classLoader: " + BaseApplication.class.getClassLoader());
	}

}
