package com.alipay.sofa.rpc.grpc.base;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class BaseApplication {

    public static void main(String[] args) {
        // 设置内嵌方式启动多模块运行容器, 也可以放在启动参数中
        System.setProperty("sofa.ark.embed.enable", "true");
        System.setProperty("sofa.ark.plugin.export.class.enable", "true");


        SpringApplication.run(BaseApplication.class, args);
    }

}
