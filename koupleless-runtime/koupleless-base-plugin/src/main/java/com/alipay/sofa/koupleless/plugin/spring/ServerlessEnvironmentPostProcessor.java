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
package com.alipay.sofa.koupleless.plugin.spring;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: yuanyuan
 * @date: 2023/10/30 9:48 下午
 */
public class ServerlessEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String                        SPRING_CONFIG_LOCATION                     = "spring.config.location";

    public static final String                        SPRING_ADDITIONAL_LOCATION                 = "spring.config.additional-location";

    // 框架定义的允许共享的配置列表
    private static final Set<String>                  DEFAULT_SHARE_KEYS                         = new HashSet<>();

    private static final Map<String, String>          COMPATIBLE_KEYS                            = new HashMap<>();

    // 允许用户扩展的配置列表
    private static final String                       ENV_SHARE_KEY                              = "ark.common.env.share.keys";
    private static final String                       MASTER_BIZ_PROPERTIES_PROPERTY_SOURCE_NAME = "MasterBiz-Config resource";
    private static final Set<String>                  BASE_APP_SHARE_ENV_KEYS                    = new HashSet<>();

    private static final AtomicReference<Environment> MASTER_ENV                                 = new AtomicReference<>();

    static {
        COMPATIBLE_KEYS.put("logging.path", "logging.file.path");
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        // 基座，先于 ark container 启动
        if (ArkClient.getMasterBiz() == null) {
            initShareEnvKeys(environment);
            return;
        }
        // 模块
        if (ArkClient.getMasterBiz().getBizClassLoader() != Thread.currentThread()
                .getContextClassLoader()) {
            // 禁用模块 spring.config.location 和 spring.config.additional-location
            String configLocation = System.getProperty(SPRING_CONFIG_LOCATION);
            String additionalLocation = System.getProperty(SPRING_ADDITIONAL_LOCATION);
            if (!StringUtils.isEmpty(configLocation) || !StringUtils.isEmpty(additionalLocation)) {
                MutablePropertySources propertySources = environment.getPropertySources();
                Iterator<PropertySource<?>> iterator = propertySources.iterator();
                Set<String> toRemove = new HashSet<>();
                while (iterator.hasNext()) {
                    PropertySource<?> next = iterator.next();
                    String psName = next.getName();
                    if (!StringUtils.isEmpty(psName) && (psName.contains(
                            getCanonicalPath(configLocation)) || psName.contains(
                            getCanonicalPath(additionalLocation)))) {
                        toRemove.add(psName);
                    }
                }
                toRemove.forEach(propertySources::remove);
                getLogger().info("disable biz additional location: {}", toRemove);
            }

            // 添加基座共享的 environment
            registerMasterBizPropertySource(MASTER_ENV.get(), environment);
        }
    }

    private void initShareEnvKeys(ConfigurableEnvironment environment) {
        // 增加框架定义的共享配置列表
        BASE_APP_SHARE_ENV_KEYS.addAll(DEFAULT_SHARE_KEYS);
        // 增加基座用户定义的共享配置列表
        BASE_APP_SHARE_ENV_KEYS.addAll(org.springframework.util.StringUtils
            .commaDelimitedListToSet(environment.getProperty(ENV_SHARE_KEY)));
        MASTER_ENV.set(environment);
        registerCompatibleProperty(environment);
    }

    /**
     * 注册兼容性配置，如果没有配置oldKey，用newKey的值代替
     * @param environment
     */
    private void registerCompatibleProperty(ConfigurableEnvironment environment) {
        //构建 compatiblePropertySource，注册到 environment 中
        Properties properties = new Properties();
        for (Entry<String, String> entry : COMPATIBLE_KEYS.entrySet()) {
            keepCompatible(environment, properties, entry.getKey(), entry.getValue());
        }
        PropertySource compatiblePropertySource = new PropertiesPropertySource(
            "compatiblePropertySource", properties);
        environment.getPropertySources().addLast(compatiblePropertySource);
        getLogger().info("register compatiblePropertySource to env,{}", properties);
    }

    private void keepCompatible(ConfigurableEnvironment environment, Properties properties,
                                String oldKey, String newKey) {
        String oldValue = environment.getProperty(oldKey);
        String newValue = environment.getProperty(newKey);
        if (StringUtils.isEmpty(oldValue) && !StringUtils.isEmpty(newValue)) {
            //如果没有配置oldKey，用newKey的值代替
            properties.put(oldKey, newValue);
        }
    }

    private void registerMasterBizPropertySource(Environment masterEnv,
                                                 ConfigurableEnvironment bizEnv) {
        //Master Biz 创建 MasterBizPropertySource 时基座已经安装完成，必然不为 null
        Assert.notNull(masterEnv, "Master biz environment is null");

        //构建 MasterBizPropertySource，注册到 environment 中
        MasterBizPropertySource masterBizPropertySource = new MasterBizPropertySource(
            MASTER_BIZ_PROPERTIES_PROPERTY_SOURCE_NAME, masterEnv, BASE_APP_SHARE_ENV_KEYS);
        bizEnv.getPropertySources().addLast(masterBizPropertySource);
        getLogger().info("register master biz property source to biz, shareKeys: {}",
            BASE_APP_SHARE_ENV_KEYS);
    }

    public String getCanonicalPath(String path) {
        try {
            if (StringUtils.isEmpty(path)) {
                return path;
            }
            File file = new File((path));
            if (file.exists() && file.isDirectory()) {
                return file.getCanonicalPath();
            }
            return path;
        } catch (Throwable t) {
            getLogger().info("Error happened when check directory for path {}", path);
            return path;
        }
    }

    /**
     * 优先级在 ConfigFileApplicationListener / ConfigDataEnvironmentPostProcessor 紧跟之后
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 11;
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(ServerlessEnvironmentPostProcessor.class);
    }
}
