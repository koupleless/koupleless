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
package com.alipay.sofa.koupleless.dubbo;

import org.apache.dubbo.config.spring.ServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

/**
 * destory all dubbo beans when spring context closed
 * if not do this, qos mode, dubbo services in uninstalled biz, will be invoked
 */

@ConditionalOnClass(ServiceBean.class)
public class ServiceBeansDestroyListener implements ApplicationListener<ContextClosedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBeansDestroyListener.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, ServiceBean> beanMap = context.getBeansOfType(ServiceBean.class);
        beanMap.forEach((name, bean) -> {
            try {
                bean.destroy();
            } catch (Exception e) {
                LOGGER.error("dubbo bean {} destroy failed", name, e);
            }
        });
    }
}
