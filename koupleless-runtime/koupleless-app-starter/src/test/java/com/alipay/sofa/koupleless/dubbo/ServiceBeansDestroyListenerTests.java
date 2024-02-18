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
    private ContextClosedEvent event;

    private Map<String, ServiceBean> serviceBeanMap = new HashMap<>();
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
