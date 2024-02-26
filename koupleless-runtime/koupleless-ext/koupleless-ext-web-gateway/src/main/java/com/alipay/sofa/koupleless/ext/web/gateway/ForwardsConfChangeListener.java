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
package com.alipay.sofa.koupleless.ext.web.gateway;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConditionalOnClass(ConfigChangeListener.class)
public class ForwardsConfChangeListener implements ConfigChangeListener, InitializingBean {
    @Autowired
    private Forwards            forwards;
    @Autowired
    private GatewayProperties   gatewayProperties;

    @Value("${apollo.bootstrap.namespaces:application}")
    private String              namespaces;

    @Autowired
    private ApplicationContext  applicationContext;
    private static final String WATCH_KEY_PREFIX = "koupleless.web.gateway";

    @Override
    public void afterPropertiesSet() throws Exception {
        String[] array = namespaces.split(",");
        for (String namespace : array) {
            ConfigService.getConfig(namespace).addChangeListener(this);
        }
    }

    @Override
    public void onChange(ConfigChangeEvent event) {
        Set<String> keys = event.changedKeys();
        boolean watchKeyChanged = false;
        for (String key : keys) {
            if (key.contains(WATCH_KEY_PREFIX)) {
                watchKeyChanged = true;
                break;
            }
        }
        if (watchKeyChanged) {
            Set<String> beanNames = applicationContext.getBeansOfType(GatewayProperties.class)
                .keySet();
            String beanName = beanNames.iterator().next();
            ConfigurationPropertiesBean bean = ConfigurationPropertiesBean.get(
                this.applicationContext, gatewayProperties, beanName);
            Bindable<?> target = bean.asBindTarget();
            ConfigurableEnvironment environment = applicationContext
                .getBean(ConfigurableEnvironment.class);
            Iterable<ConfigurationPropertySource> configurationPropertySources = ConfigurationPropertySources
                .from(environment.getPropertySources());
            Binder binder = new Binder(configurationPropertySources);
            binder.bind(WATCH_KEY_PREFIX, target);
            ForwardItems.init(forwards, gatewayProperties);
        }
    }
}
