package com.alipay.sofa.biz1;

import com.alipay.sofa.biz1.mapper.StudentMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan(basePackages = "com.alipay.sofa.biz1.mapper")
public class Biz1Application {
    private static Logger LOGGER = LoggerFactory.getLogger(Biz1Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Biz1Application.class, args);

        StudentMapper studentMapper = (StudentMapper) context.getBean("studentMapper");
        studentMapper.getAll();

        LOGGER.info("Biz1Application start!");
        LOGGER.info("Spring Boot Version: " + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("Biz1Application classLoader: " + Biz1Application.class.getClassLoader());
    }
}
