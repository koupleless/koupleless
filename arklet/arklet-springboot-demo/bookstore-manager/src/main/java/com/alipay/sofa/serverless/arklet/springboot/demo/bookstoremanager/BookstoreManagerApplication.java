package com.alipay.sofa.serverless.arklet.springboot.demo.bookstoremanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Lunarscave
 */
@SpringBootApplication
public class BookstoreManagerApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext applicationContext =  SpringApplication.run(BookstoreManagerApplication.class, args);
    }

}
