/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.serverless.arklet.springboot.starter.listener;

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

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 * @author gouzhendong.gzd
 * @version $Id: ArkletApplicationListenerTest, v 0.1 2023-11-20 20:45 gouzhendong.gzd Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class ArkletApplicationListenerTest {

    @InjectMocks
    private ArkletApplicationListener arkletApplicationListener;

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
        }

        arkletApplicationListener.combineDeployFromLocalDir();

        {
            verify(operationService, times(1)).combineInstall(any(CombineInstallRequest.class));
        }

    }

}
