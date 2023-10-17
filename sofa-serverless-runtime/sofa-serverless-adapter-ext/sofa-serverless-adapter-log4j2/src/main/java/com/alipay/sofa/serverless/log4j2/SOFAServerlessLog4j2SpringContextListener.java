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
package com.alipay.sofa.serverless.log4j2;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持将配置转换为 log4j2 context.
 *
 * @author ruoshan
 * @version $Id: AlipayLog4j2SpringContextListener.java, v 0.1 2018年08月23日 10:32 PM ruoshan Exp $
 */
public class SOFAServerlessLog4j2SpringContextListener implements GenericApplicationListener,
                                                      Ordered {

    private static Class<?>[] EVENT_TYPES  = { ApplicationStartingEvent.class,
            ApplicationEnvironmentPreparedEvent.class };

    private static Class<?>[] SOURCE_TYPES = { SpringApplication.class, ApplicationContext.class };

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return isAssignableFrom(eventType.getRawClass(), EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            onApplicationStartingEvent();
        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
        }
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        Map<String, String> configMap = new ConcurrentHashMap<>();
        Set<String> configKeys = new HashSet<>();
        environment.getPropertySources().stream().filter(propertySource -> propertySource instanceof EnumerablePropertySource)
                .forEach(propertySource -> configKeys.addAll(Arrays.asList(((EnumerablePropertySource<?>)propertySource).getPropertyNames())));

        configKeys.forEach(key -> {
            try {
                configMap.put(key, environment.getProperty(key));
            } catch (Throwable t) {
                // ignore, 异常在 AlipayPrintEnvironmentListener 中打印
            }
        });

        configMap.forEach(ThreadContext::put);
    }

    private void onApplicationStartingEvent() {
        System.setProperty(LoggingSystem.class.getName(),
            SOFAServerlessLog4j2LoggingSystem.class.getName());
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 19;
    }

    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            for (Class<?> supportedType : supportedTypes) {
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }
}
