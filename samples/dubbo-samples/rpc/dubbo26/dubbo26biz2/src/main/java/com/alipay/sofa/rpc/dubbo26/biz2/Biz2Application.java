/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.rpc.dubbo26.biz2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 *
 * @author syd
 * @version Biz2Application.java, v 0.1 2023年10月31日 19:42 syd
 */

@SpringBootApplication
@ImportResource("classpath:provider.xml")
public class Biz2Application {
    private static Logger LOGGER = LoggerFactory.getLogger(Biz2Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Biz2Application.class, args);

        LOGGER.info("Biz2Application start!");
        LOGGER.info("Spring Boot Version: "
                    + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("Biz2Application classLoader: " + Biz2Application.class.getClassLoader());
    }

}