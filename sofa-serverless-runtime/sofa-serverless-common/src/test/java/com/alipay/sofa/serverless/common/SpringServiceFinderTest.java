package com.alipay.sofa.serverless.common;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.serverless.common.api.SpringServiceFinder;
import com.alipay.sofa.serverless.common.environment.ConditionalOnMasterBiz;
import com.alipay.sofa.serverless.common.environment.ConditionalOnNotMasterBiz;
import com.alipay.sofa.serverless.common.exception.BizRuntimeException;
import com.alipay.sofa.serverless.common.exception.ErrorCodes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

/**
 * @author: yuanyuan
 * @date: 2023/9/26 9:40 下午
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringServiceFinderTest {

    @Mock
    private Biz masterBiz;

    @Mock
    private Biz biz1;

    @Mock
    private BizManagerService bizManagerService;

    @Test
    public void testSpringServiceFinder() {
        ArkClient.setMasterBiz(masterBiz);
        Mockito.when(masterBiz.getBizState()).thenReturn(BizState.ACTIVATED);

        ArkClient.setBizManagerService(bizManagerService);
        Mockito.when(bizManagerService.getBiz("biz1", "version1")).thenReturn(biz1);

        Properties properties = new Properties();
        properties.setProperty("spring.application.name", "biz1");
        SpringApplication springApplication = new SpringApplication(CustomConfiguration.class);
        springApplication.setDefaultProperties(properties);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext bizContext = springApplication.run();
        bizContext.getBeanFactory().registerSingleton("moduleBean", new ModuleBean());

        BaseBean baseBean = SpringServiceFinder.getBaseService("baseBean");
        BaseBean baseBean1 = SpringServiceFinder.getBaseService(BaseBean.class);
        Map<String, BaseBean> baseBeanMap = SpringServiceFinder.listBaseServices(BaseBean.class);
        ModuleBean moduleBean = SpringServiceFinder.getModuleService("biz1", "version1", "moduleBean");
        ModuleBean moduleBean1 = SpringServiceFinder.getModuleService("biz1", "version1", ModuleBean.class);
        Map<String, ModuleBean> moduleBeanMap = SpringServiceFinder.listModuleServices("biz1", "version1", ModuleBean.class);
    }

    @Configuration
    public static class CustomConfiguration {

    }

    public class BaseBean {
    }


    public class ModuleBean {
    }

}
