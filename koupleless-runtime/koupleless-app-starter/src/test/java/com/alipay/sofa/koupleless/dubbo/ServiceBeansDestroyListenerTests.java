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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.util.HashMap;
import java.util.Map;

public class ServiceBeansDestroyListenerTests {
    private ContextClosedEvent          event;

    private Map<String, ServiceBean>    serviceBeanMap              = new HashMap<>();
    private ServiceBeansDestroyListener serviceBeansDestroyListener = new ServiceBeansDestroyListener();

    @Before
    public void before() {
        for (int i = 0; i < 10; i++) {
            ServiceBean serviceBean = Mockito.mock(ServiceBean.class);
            serviceBeanMap.put("serviceBean" + i, serviceBean);
        }
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        Mockito.when(context.getBeansOfType(ServiceBean.class)).thenReturn(serviceBeanMap);
        event = Mockito.mock(ContextClosedEvent.class);
        Mockito.when(event.getApplicationContext()).thenReturn(context);
    }

    @Test
    public void test() throws Exception {
        serviceBeansDestroyListener.onApplicationEvent(event);
        Assert.assertFalse(serviceBeanMap.isEmpty());
        for (ServiceBean serviceBean : serviceBeanMap.values()) {
            Mockito.verify(serviceBean, Mockito.atLeastOnce()).destroy();
        }
    }
}
