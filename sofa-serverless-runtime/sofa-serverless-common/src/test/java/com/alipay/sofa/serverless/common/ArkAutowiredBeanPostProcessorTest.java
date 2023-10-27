package com.alipay.sofa.serverless.common;


import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.serverless.common.api.AutowiredFromBase;
import com.alipay.sofa.serverless.common.api.AutowiredFromBiz;
import com.alipay.sofa.serverless.common.service.ArkAutowiredBeanPostProcessor;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: yuanyuan
 * @date: 2023/10/27 4:43 下午
 */
public class ArkAutowiredBeanPostProcessorTest {

    @Before
    public void prepare() {
        Biz masterBiz = mock(Biz.class);
        when(masterBiz.getBizName()).thenReturn("master");
        when(masterBiz.getBizVersion()).thenReturn("1.0.0");
        when(masterBiz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        when(masterBiz.getBizState()).thenReturn(BizState.ACTIVATED);
        ArkClient.setMasterBiz(masterBiz);

        Biz biz = mock(Biz.class);
        when(biz.getBizName()).thenReturn("biz1");
        when(biz.getBizVersion()).thenReturn("version1");
        when(biz.getBizClassLoader()).thenReturn(new URLClassLoader(new URL[0]));
        when(biz.getBizState()).thenReturn(BizState.ACTIVATED);

        BizManagerService bizManagerService = mock(BizManagerService.class);
        ArkClient.setBizManagerService(bizManagerService);
        when(bizManagerService.getBiz("master", "1.0.0")).thenReturn(masterBiz);
        when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz);
    }

    @Test
    public void testArkAutowired() {
        TestBean testBean = new TestBean();
        ArkAutowiredBeanPostProcessor arkAutowiredBeanPostProcessor = new ArkAutowiredBeanPostProcessor();
        Object testBean1 = arkAutowiredBeanPostProcessor.postProcessBeforeInitialization(testBean, "testBean");
    }


    public static class TestBean {

        @AutowiredFromBase
        private String baseBean;

        @AutowiredFromBiz(bizName = "biz1", bizVersion = "version1")
        private String bizBean;

    }

}

