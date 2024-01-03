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
package com.alipay.sofa.serverless.support.dubbo;

import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author: yuanyuan
 * @date: 2023/12/25 4:14 下午
 */
public class BizDubboBootstrapListener implements ApplicationListener {

    private final DubboBootstrap dubboBootstrap;

    //    private final ConfigManager configManager;
    //    private final Environment environment;

    public BizDubboBootstrapListener() {
        this.dubboBootstrap = DubboBootstrap.getInstance();
        //        this.configManager = ApplicationModel.getConfigManager();
        //        this.environment = ApplicationModel.getEnvironment();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (Thread.currentThread().getContextClassLoader() == this.getClass().getClassLoader()) {
            return;
        }
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        try {
            ReflectionUtils.getMethod(DubboBootstrap.class, "exportServices")
                .invoke(dubboBootstrap);
            ReflectionUtils.getMethod(DubboBootstrap.class, "referServices").invoke(dubboBootstrap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
