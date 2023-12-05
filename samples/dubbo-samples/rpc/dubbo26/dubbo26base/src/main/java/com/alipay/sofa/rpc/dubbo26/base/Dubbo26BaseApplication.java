/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo26.base;

import java.io.File;

import com.alipay.sofa.ark.api.ArkClient;
import org.apache.commons.io.FileUtils;
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
     * 方便本地测试用，启动基座时，默认也启动模块
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        try {
            installBiz("dubbo26biz/target/dubbo26biz-0.0.1-SNAPSHOT-ark-biz.jar");
            installBiz("dubbo26biz2/target/dubbo26biz2-0.0.1-SNAPSHOT-ark-biz.jar");
        } catch (Throwable e) {
            LOGGER.error("Install biz failed", e);
        }
    }

    protected void installBiz(String bizDir) throws Throwable {
        String pathRoot = "rpc/dubbo26/";
        File bizFile = new File(pathRoot + bizDir);
        if (bizFile.exists()) {
            File tmpFile = new File(pathRoot + "target/" + bizFile.getName());
            if(tmpFile.exists()){
                tmpFile.delete();
            }
            FileUtils.copyFile(bizFile, tmpFile);
            ArkClient.installBiz(tmpFile);
        } else {
            LOGGER.warn(bizFile.getAbsolutePath() + " do not exist");
        }
    }
}