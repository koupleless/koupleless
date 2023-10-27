package com.alipay.sofa.serverless.plugin.manager.handler;

import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.common.BizRuntimeContextRegistry;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: yuanyuan
 * @date: 2023/10/27 6:22 下午
 */
public class BeforeBizStartupEventHandlerTest {

    @Test
    public void test() {
        BeforeBizStartupEventHandler beforeBizStartupEventHandler = new BeforeBizStartupEventHandler();
        Biz biz = mock(Biz.class);
        when(biz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        BeforeBizStartupEvent beforeBizStartupEvent = new BeforeBizStartupEvent(biz);
        beforeBizStartupEventHandler.handleEvent(beforeBizStartupEvent);
        Assert.assertNotNull(BizRuntimeContextRegistry.getBizRuntimeContext(biz));
    }
}
