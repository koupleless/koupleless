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
package com.alipay.sofa.koupleless.plugin.manager.handler;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.plugin.BaseRuntimeAutoConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;

@RunWith(MockitoJUnitRunner.class)
public class BizUninstallEventHandlerTest {
    private static final String            bizName = "eventHandlerTest";
    @Mock
    private Biz                            biz;

    @Mock
    private BizManagerService              bizManagerService;

    private ConfigurableApplicationContext ctx;

    private SpringApplication              springApplication;

    @Before
    public void before() {
        Properties properties = new Properties();
        properties.setProperty("spring.application.name", bizName);

        ArkClient.setBizManagerService(bizManagerService);

        springApplication = new SpringApplication(BaseRuntimeAutoConfiguration.class);
        springApplication.setDefaultProperties(properties);

        springApplication.setWebApplicationType(WebApplicationType.NONE);
    }

    @Test
    public void handleEvent() {
        Mockito.when(biz.getBizName()).thenReturn(bizName);
        Mockito.when(biz.getBizClassLoader()).thenReturn(this.getClass().getClassLoader());

        BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(biz);
        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntimeContext);

        ctx = springApplication.run();

        Assert.assertEquals(bizRuntimeContext.getRootApplicationContext(), ctx);

        BizUninstallEventHandler bizUninstallEventHandler = new BizUninstallEventHandler();
        bizUninstallEventHandler.handleEvent(new BeforeBizStopEvent(biz));
        Assert.assertFalse(ctx.isActive());
    }

}
