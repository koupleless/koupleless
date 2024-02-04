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
package com.alipay.sofa.rpc.dubbo3.grpcbiz;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class GrpcBizApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(GrpcBizApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GrpcBizApplication.class, args);

        LOGGER.info("GrpcBizApplication start!");
        LOGGER.info("Spring Boot Version: "
                    + SpringApplication.class.getPackage().getImplementationVersion());
        LOGGER.info("GrpcBizApplication classLoader: " + GrpcBizApplication.class.getClassLoader());
    }

}
