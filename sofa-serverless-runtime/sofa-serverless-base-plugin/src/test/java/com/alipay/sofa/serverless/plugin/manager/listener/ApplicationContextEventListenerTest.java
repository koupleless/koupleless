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
    private UnifiedOperationService         operationService;

    MockedStatic<ArkletComponentRegistry>   componentRegistryMockedStatic = null;

    @Before
    public void beforeTest() {
        componentRegistryMockedStatic = mockStatic(ArkletComponentRegistry.class);
        System.setProperty("sofa.ark.deploy.combine.biz.dir.absolute", "/path/to/dir");
    }

    @After
    public void afterTest() {
        componentRegistryMockedStatic.close();
        System.clearProperty("sofa.ark.deploy.combine.biz.dir.absolute");
    }

    @SneakyThrows
    @Test
    public void testCombineDeployFromLocalDir() {
        CombineInstallResponse response = null;
        ContextRefreshedEvent event = null;
        {
            componentRegistryMockedStatic.when(ArkletComponentRegistry::getOperationServiceInstance).thenReturn(operationService);

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
