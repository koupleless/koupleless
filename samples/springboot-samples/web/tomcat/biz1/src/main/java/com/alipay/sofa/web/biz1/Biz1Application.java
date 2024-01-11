package com.alipay.sofa.web.biz1;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

@SpringBootApplication
public class Biz1Application {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Biz1Application.class);

        // set biz to use resource loader.
        ResourceLoader resourceLoader = new DefaultResourceLoader(Biz1Application.class.getClassLoader());
        builder.resourceLoader(resourceLoader);
        builder.build().run(args);
    }

}
