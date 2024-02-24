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
package com.alipay.sofa.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class BaseApplication {

    public static void main(String[] args) {
        System.setProperty("koupleless.service.lazy.init.enable", "true");

        SpringApplication.run(BaseApplication.class, args);
        System.out.println("SofaArkSpringGuidesApplication start!");
        System.out.println("Spring Boot Version: "
                           + SpringApplication.class.getPackage().getImplementationVersion());
        System.out.println("SofaArkSpringGuidesApplication classLoader: "
                           + BaseApplication.class.getClassLoader());
    }
}
