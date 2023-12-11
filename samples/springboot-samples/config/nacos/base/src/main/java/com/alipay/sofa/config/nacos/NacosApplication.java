package com.alipay.sofa.config.nacos;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@NacosPropertySource(dataId = "base", autoRefreshed = true)
@SpringBootApplication
public class NacosApplication {

    public static void main(String[] args) {

        SpringApplication.run(NacosApplication.class, args);
    }
}