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
package com.alipay.sofa.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

@SpringBootApplication(exclude = { JacksonAutoConfiguration.class,
                                  DataSourceAutoConfiguration.class })
public class BizApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BizApplication.class)
            .web(WebApplicationType.SERVLET);

        // set biz to use resource loader.
        ResourceLoader resourceLoader = new DefaultResourceLoader(
            BizApplication.class.getClassLoader());
        builder.resourceLoader(resourceLoader);

        ConfigurableApplicationContext context = builder.build().run(args);
        System.out.println("Biz start!");
        System.out.println("Biz spring boot version: "
                           + SpringApplication.class.getPackage().getImplementationVersion());
        System.out.println("Biz classLoader: " + BizApplication.class.getClassLoader());

    }
}
