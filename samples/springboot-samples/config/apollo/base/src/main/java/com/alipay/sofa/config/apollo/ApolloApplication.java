package com.alipay.sofa.config.apollo;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@EnableApolloConfig
@SpringBootApplication
public class ApolloApplication {

    public static void main(String[] args) {
        // 默认 apollo 使用 Eureka 获取服务地址，由于本地 docker 采用 bridge 网络模式，通过 Eureka 获取到的是虚拟子网服务地址
        // 在本地无法直接调用，所以这里直接通过自定义配置 `apollo.configService` 指定为 localhost

        System.setProperty("apollo.configService", "http://localhost:8080");
        System.setProperty("apollo.config-service", "http://localhost:8080");
        System.setProperty("env", "DEV");

        SpringApplication.run(ApolloApplication.class, args);
    }
}
