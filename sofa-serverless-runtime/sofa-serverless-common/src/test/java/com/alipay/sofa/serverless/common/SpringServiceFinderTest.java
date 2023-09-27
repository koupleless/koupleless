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
package com.alipay.sofa.serverless.common;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.serverless.common.api.SpringServiceFinder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Properties;

/**
 * @author: yuanyuan
 * @date: 2023/9/26 9:40 下午
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringServiceFinderTest {

    @Mock
    private Biz               masterBiz;

    @Mock
    private Biz               biz1;

    @Mock
    private BizManagerService bizManagerService;

    @Test
    public void testSpringServiceFinder() {
        ConfigurableApplicationContext masterCtx = buildApplicationContext("masterBiz");
        masterCtx.getBeanFactory().registerSingleton("baseBean", new BaseBean());

        ConfigurableApplicationContext bizCtx = buildApplicationContext("biz1");
        bizCtx.getBeanFactory().registerSingleton("moduleBean", new ModuleBean());

        ArkClient.setBizManagerService(bizManagerService);

        ArkClient.setMasterBiz(masterBiz);
        Mockito.when(bizManagerService.getBiz("master", "1.0.0")).thenReturn(masterBiz);
        Mockito.when(masterBiz.getBizState()).thenReturn(BizState.ACTIVATED);
        Mockito.when(masterBiz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        Mockito.when(masterBiz.getBizName()).thenReturn("master");
        Mockito.when(masterBiz.getBizVersion()).thenReturn("1.0.0");
        BizRuntimeContext masterBizRuntime = new BizRuntimeContext(masterBiz, masterCtx);
        BizRuntimeContextRegistry.registerSpringContext(masterBizRuntime);

        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        Mockito.when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);
        Mockito.when(biz1.getBizClassLoader()).thenReturn(new URLClassLoader(new URL[0]));
        Mockito.when(biz1.getBizName()).thenReturn("biz1");
        Mockito.when(biz1.getBizVersion()).thenReturn("version1");
        BizRuntimeContext bizRuntime = new BizRuntimeContext(biz1, bizCtx);
        BizRuntimeContextRegistry.registerSpringContext(bizRuntime);

        BaseBean baseBean = SpringServiceFinder.getBaseService("baseBean");
        Assert.assertNotNull(baseBean);
        BaseBean baseBean1 = SpringServiceFinder.getBaseService(BaseBean.class);
        Assert.assertNotNull(baseBean1);
        Map<String, BaseBean> baseBeanMap = SpringServiceFinder.listBaseServices(BaseBean.class);
        Assert.assertNotNull(baseBeanMap);
        Assert.assertEquals(1, baseBeanMap.size());
        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean");
        Assert.assertNotNull(moduleBean);
        ModuleBean moduleBean1 = SpringServiceFinder.getModuleService("biz1", "version1",
            ModuleBean.class);
        Assert.assertNotNull(moduleBean1);
        Map<String, ModuleBean> moduleBeanMap = SpringServiceFinder.listModuleServices("biz1",
            "version1", ModuleBean.class);
        Assert.assertNotNull(moduleBeanMap);
        Assert.assertEquals(1, moduleBeanMap.size());

        Assert.assertEquals("base", baseBean.test());
        Assert.assertEquals("module", moduleBean.test());
    }

    public ConfigurableApplicationContext buildApplicationContext(String appName) {
        Properties properties = new Properties();
        properties.setProperty("spring.application.name", appName);
        SpringApplication springApplication = new SpringApplication(CustomConfiguration.class);
        springApplication.setDefaultProperties(properties);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        return springApplication.run();
    }

    @Configuration
    public static class CustomConfiguration {

    }

    public class BaseBean {

        public String test() {
            return "base";
        }
    }

    public class ModuleBean {

        public String test() {
            return "module";
        }
    }

}
