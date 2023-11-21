/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.serverless.plugin.manager.listener;

import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.common.model.CombineInstallRequest;
import com.alipay.sofa.serverless.arklet.core.common.model.CombineInstallResponse;
import com.alipay.sofa.serverless.arklet.core.ops.UnifiedOperationService;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 *
 * @author gouzhendong.gzd
 * @version $Id: ApplicationContextEventListenerTest, v 0.1 2023-11-21 11:32 gouzhendong.gzd Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationContextEventListenerTest {

    @InjectMocks
    private ApplicationContextEventListener arkletApplicationListener;

    @Mock
    private UnifiedOperationService operationService;

    MockedStatic<ArkletComponentRegistry> componentRegistryMockedStatic = null;

    @Before
    public void beforeTest() {
        componentRegistryMockedStatic = mockStatic(ArkletComponentRegistry.class);
    }

    @After
    public void afterTest() {
        componentRegistryMockedStatic.close();
    }

    @SneakyThrows
    @Test
    public void testCombineDeployFromLocalDir() {
        CombineInstallResponse response = null;
        ContextRefreshedEvent event = null;
        {
            componentRegistryMockedStatic.when(ArkletComponentRegistry::getOperationServiceInstance).thenReturn(operationService);
            System.setProperty("deploy.combine.biz.dir.absolute.path", "/path/to/dir");

            response = CombineInstallResponse.builder().
                    code(ResponseCode.SUCCESS).
                    bizUrlToResponse(new HashMap<>()).
                    build();

            doReturn(response).when(operationService).combineInstall(CombineInstallRequest.builder().
                    bizDirAbsolutePath("/path/to/dir").
                    build());

            event = mock(ContextRefreshedEvent.class);
            doReturn(mock(ApplicationContext.class)).when(event).getApplicationContext();
        }

        arkletApplicationListener.onApplicationEvent(event);

        {
            verify(operationService, times(1)).combineInstall(any(CombineInstallRequest.class));
        }

    }
}
