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
package com.alipay.sofa.koupleless.support.dubbo;

import org.apache.dubbo.config.ServiceConfigBase;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.model.ServiceRepository;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author: yuanyuan
 * @date: 2023/12/25 4:14 下午
 */
public class BizDubboBootstrapListener implements ApplicationListener {

    private final DubboBootstrap    dubboBootstrap;

    private final ConfigManager     configManager;
    private final ServiceRepository serviceRepository;

    //    private final Environment environment;

    public BizDubboBootstrapListener() {
        this.dubboBootstrap = DubboBootstrap.getInstance();
        this.configManager = ApplicationModel.getConfigManager();
        this.serviceRepository = ApplicationModel.getServiceRepository();
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
        if (event instanceof ContextClosedEvent) {
            onContextClosedEvent((ContextClosedEvent) event);
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

    private void onContextClosedEvent(ContextClosedEvent event) {
        // DubboBootstrap.unexportServices 会 unexport 所有服务，只需要 unexport 当前 biz 的服务即可
        Map<String, ServiceConfigBase<?>> exportedServices = ReflectionUtils.getField(
            dubboBootstrap, DubboBootstrap.class, "exportedServices");

        Set<String> bizUnexportServices = new HashSet<>();
        for (Map.Entry<String, ServiceConfigBase<?>> entry : exportedServices.entrySet()) {
            String serviceKey = entry.getKey();
            ServiceConfigBase<?> sc = entry.getValue();
            if (sc.getRef().getClass().getClassLoader() == Thread.currentThread()
                .getContextClassLoader()) {
                bizUnexportServices.add(serviceKey);
                configManager.removeConfig(sc);
                sc.unexport();

                serviceRepository.unregisterService(sc.getUniqueServiceName());
            }
        }
        for (String service : bizUnexportServices) {
            exportedServices.remove(service);
        }

    }
}
