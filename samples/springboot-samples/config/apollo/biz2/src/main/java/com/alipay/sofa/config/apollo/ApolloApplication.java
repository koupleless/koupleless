package com.alipay.sofa.config.apollo;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

@EnableConfigurationProperties
@EnableApolloConfig
@SpringBootApplication
public class ApolloApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ApolloApplication.class);

        // set biz to use resource loader.
        ResourceLoader resourceLoader = new DefaultResourceLoader(ApolloApplication.class.getClassLoader());
        builder.resourceLoader(resourceLoader);
        builder.build().run(args);
    }
}
