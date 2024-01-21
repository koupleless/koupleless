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
package com.alipay.sofa.koupleless.common;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.koupleless.common.api.AutowiredFromBase;
import com.alipay.sofa.koupleless.common.api.AutowiredFromBiz;
import com.alipay.sofa.koupleless.common.api.SpringBeanFinder;
import com.alipay.sofa.koupleless.common.api.SpringServiceFinder;
import com.alipay.sofa.koupleless.common.service.ArkAutowiredBeanPostProcessor;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * @author: yuanyuan
 * @date: 2023/9/26 9:40 下午
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringServiceAndBeanFinderTest {

    @Mock
    private Biz               masterBiz;

    @Mock
    private Biz               biz1;

    @Mock
    private Biz               biz2;

    @Mock
    private BizManagerService bizManagerService;

    @Before
    public void prepare() {
        ConfigurableApplicationContext masterCtx = buildApplicationContext("masterBiz");
        masterCtx.getBeanFactory().registerSingleton("baseBean", new BaseBean());

        ConfigurableApplicationContext bizCtx = buildApplicationContext("biz1");
        bizCtx.getBeanFactory().registerSingleton("moduleBean", new ModuleBean());

        ArkClient.setBizManagerService(bizManagerService);

        ArkClient.setMasterBiz(masterBiz);
        when(bizManagerService.getBiz("master", "1.0.0")).thenReturn(masterBiz);
        when(masterBiz.getBizState()).thenReturn(BizState.ACTIVATED);
        when(masterBiz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        when(masterBiz.getBizName()).thenReturn("master");
        when(masterBiz.getBizVersion()).thenReturn("1.0.0");
        BizRuntimeContext masterBizRuntime = new BizRuntimeContext(masterBiz, masterCtx);
        BizRuntimeContextRegistry.registerBizRuntimeManager(masterBizRuntime);

        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);
        when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);
        when(biz1.getBizClassLoader()).thenReturn(new URLClassLoader(new URL[0]));
        when(biz1.getBizName()).thenReturn("biz1");
        when(biz1.getBizVersion()).thenReturn("version1");
        BizRuntimeContext bizRuntime = new BizRuntimeContext(biz1, bizCtx);
        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntime);
    }

    @Test
    public void testSpringServiceFinder() {
        BaseBean baseBean = SpringServiceFinder.getBaseService("baseBean", BaseBean.class);
        Assert.assertNotNull(baseBean);
        BaseBean baseBean1 = SpringServiceFinder.getBaseService(BaseBean.class);
        Assert.assertNotNull(baseBean1);
        Map<String, BaseBean> baseBeanMap = SpringServiceFinder.listBaseServices(BaseBean.class);
        Assert.assertNotNull(baseBeanMap);
        Assert.assertEquals(1, baseBeanMap.size());
        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1",
            "moduleBean", ModuleBean.class);
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

        // test to invoke crossing classloader

        URL url = SpringServiceAndBeanFinderTest.class.getClassLoader().getResource("");
        URLClassLoader loader = new URLClassLoader(new URL[] { url }, null);
        Object newModuleBean = null;
        try {
            Class<?> aClass = loader
                .loadClass("com.alipay.sofa.koupleless.common.SpringServiceAndBeanFinderTest$ModuleBean");
            newModuleBean = aClass.newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }
        ConfigurableApplicationContext biz2Ctx = buildApplicationContext("biz2");
        biz2Ctx.getBeanFactory().registerSingleton("moduleBean", newModuleBean);
        Mockito.when(bizManagerService.getBiz("biz2", "version1")).thenReturn(biz2);
        Mockito.when(biz2.getBizState()).thenReturn(BizState.ACTIVATED);
        Mockito.when(biz2.getBizClassLoader()).thenReturn(loader);
        Mockito.when(biz2.getBizName()).thenReturn("biz2");
        Mockito.when(biz2.getBizVersion()).thenReturn("version1");
        BizRuntimeContext biz2Runtime = new BizRuntimeContext(biz2, biz2Ctx);
        BizRuntimeContextRegistry.registerBizRuntimeManager(biz2Runtime);
        ModuleBean foundModuleBean = SpringServiceFinder.getModuleService("biz2", "version1",
            "moduleBean", ModuleBean.class);
        Assert.assertNotNull(foundModuleBean);
        Assert.assertEquals(newModuleBean.toString(), foundModuleBean.toString());
        Assert.assertEquals("module", foundModuleBean.test());

    }

    @Test
    public void testArkAutowired() {
        ModuleBean moduleBean = new ModuleBean();
        ArkAutowiredBeanPostProcessor arkAutowiredBeanPostProcessor = new ArkAutowiredBeanPostProcessor();
        Object testBean = arkAutowiredBeanPostProcessor.postProcessBeforeInitialization(moduleBean,
            "moduleBean");
        Assert.assertNotNull(testBean);
        Assert.assertEquals(moduleBean, testBean);
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBean")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBeanList")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBeanSet")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "baseBeanMap")),
            testBean));
        Assert.assertNotNull(ReflectionUtils.getField(
            Objects.requireNonNull(ReflectionUtils.findField(ModuleBean.class, "moduleBean")),
            testBean));
    }

    @Test
    public void testGetBaseBean() {
        Object baseBean = SpringBeanFinder.getBaseBean("baseBean");
        BaseBean baseBean1 = SpringBeanFinder.getBaseBean(BaseBean.class);
        Assert.assertNotNull(baseBean);
        Assert.assertNotNull(baseBean1);
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

    public static class BaseBean {

        public String test() {
            return "base";
        }
    }

    public static class ModuleBean {

        @AutowiredFromBase
        private BaseBean              baseBean;

        @AutowiredFromBase
        private List<BaseBean>        baseBeanList;

        @AutowiredFromBase
        private Set<BaseBean>         baseBeanSet;

        @AutowiredFromBase
        private Map<String, BaseBean> baseBeanMap;

        @AutowiredFromBiz(bizName = "biz1", bizVersion = "version1")
        private ModuleBean            moduleBean;

        public String test() {
            return "module";
        }
    }

}
