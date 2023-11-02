package com.alipay.sofa.serverless.common;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.serverless.common.spring.ServerlessApplicationListener;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;

import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_EXPORT_CLASS_ENABLE;
import static org.mockito.Mockito.mock;

/**
 * @author: yuanyuan
 * @date: 2023/11/2 9:39 下午
 */
public class ServerlessApplicationListenerTest {

    @Test
    public void testOnApplicationEvent() {
        ServerlessApplicationListener listener = new ServerlessApplicationListener();
        ApplicationStartingEvent applicationStartingEvent = new ApplicationStartingEvent(mock(ConfigurableBootstrapContext.class), mock(SpringApplication.class), new String[0]);
        listener.onApplicationEvent(applicationStartingEvent);
        Assert.assertTrue(ArkConfigs.isEmbedEnable());
        Assert.assertEquals("true", System.getProperty(PLUGIN_EXPORT_CLASS_ENABLE));
    }
}
