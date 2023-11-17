/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo26.base;

import java.io.File;

import com.alipay.sofa.ark.api.ArkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 *
 * @author syd
 * @version BaseApplication.java, v 0.1 2023年10月31日 19:27 syd
 */

@SpringBootApplication
@ImportResource("classpath:provider.xml")
public class Dubbo26BaseApplication implements CommandLineRunner {
    private static Logger LOGGER = LoggerFactory.getLogger(Dubbo26BaseApplication.class);

    public static void main(String[] args) {

        //Prevent to get IPV6 address,this way only work in debug mode
        //But you can pass use -Djava.net.preferIPv4Stack=true,then it work well whether in debug mode or not
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(Dubbo26BaseApplication.class, args);
    }

    /**
     * Install biz when base started
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        File biz1 = new File("./rpc/dubbo26/dubbo26biz/target/dubbo26biz-0.0.1-SNAPSHOT-ark-biz.jar");
        File biz2 = new File("./rpc/dubbo26/dubbo26biz2/target/dubbo26biz2-0.0.1-SNAPSHOT-ark-biz.jar");
        try {
            if (biz1.exists()) {
                ArkClient.installBiz(biz1);
            } else {
                LOGGER.warn(biz1.getAbsolutePath() + " do not exist");
            }
            if (biz2.exists()) {
                ArkClient.installBiz(biz2);
            } else {
                LOGGER.warn(biz2.getAbsolutePath() + " do not exist");
            }
        } catch (Throwable e) {
            LOGGER.error("Install biz failed", e);
        }
    }
}