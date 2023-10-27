package com.alipay.sofa.serverless.plugin;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.common.BizRuntimeContext;
import com.alipay.sofa.serverless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.serverless.common.service.ArkAutowiredBeanPostProcessor;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: yuanyuan
 * @date: 2023/10/27 4:47 下午
 */
public class BaseRuntimeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BaseRuntimeAutoConfiguration.class));

    @Before
    public void prepare() {
        Biz biz = mock(Biz.class);
        when(biz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(biz);
        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntimeContext);
    }

    @Test
    public void test() {
        contextRunner.run(context -> {
            Assertions.assertThat(context).hasSingleBean(BizRuntimeContext.class);
            Assertions.assertThat(context).doesNotHaveBean(ArkAutowiredBeanPostProcessor.class);
        });
    }
}
