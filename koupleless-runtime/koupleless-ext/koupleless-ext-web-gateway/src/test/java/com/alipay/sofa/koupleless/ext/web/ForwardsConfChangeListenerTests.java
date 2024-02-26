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
package com.alipay.sofa.koupleless.ext.web;

import com.alipay.sofa.koupleless.ext.web.gateway.ForwardItem;
import com.alipay.sofa.koupleless.ext.web.gateway.Forwards;
import com.alipay.sofa.koupleless.ext.web.gateway.ForwardsConfChangeListener;
import com.alipay.sofa.koupleless.ext.web.gateway.GatewayProperties;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ForwardsConfChangeListenerTests {
    @InjectMocks
    private ForwardsConfChangeListener listener;
    @Mock
    private ApplicationContext         applicationContext;
    @Spy
    private GatewayProperties          gatewayProperties;
    @Mock
    private ConfigurableEnvironment    environment;

    private Map<String, Object>        sourceMap = new HashMap<>();

    @Spy
    private Forwards                   forwards  = new Forwards();

    @Before
    public void before() {
        Mockito.when(applicationContext.getBeansOfType(GatewayProperties.class)).thenReturn(
            Collections.singletonMap("gatewayProperties", gatewayProperties));
        Mockito.when(applicationContext.getBean(ConfigurableEnvironment.class)).thenReturn(
            environment);

        MutablePropertySources sources = new MutablePropertySources();

        PropertySource source = new MapPropertySource("xxx", sourceMap);
        sources.addLast(source);
        Mockito.when(environment.getPropertySources()).thenReturn(sources);

    }

    @Test
    public void testOnChange() {
        ConfigChangeEvent event = Mockito.mock(ConfigChangeEvent.class);
        Mockito.when(event.changedKeys()).thenReturn(
            Collections.singleton("koupleless.web.gateway.forwards[0].contextPath"));

        sourceMap.put("koupleless.web.gateway.forwards[0].contextPath", "a");
        sourceMap.put("koupleless.web.gateway.forwards[0].paths[0].from", "x");
        sourceMap.put("koupleless.web.gateway.forwards[0].paths[0].to", "ax");
        listener.onChange(event);
        ForwardItem item = forwards.getForwardItem("xxx", "/x/a/b/c");
        Assert.assertEquals(item.getContextPath(), "/a");
        Assert.assertEquals(item.getTo(), "/ax");
    }
}
