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
package com.alipay.sofa.koupleless.plugin.manager.listener;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;
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
 * @author CodeNoobKingKc2
 * @version $Id: ApplicationContextEventListenerTest, v 0.1 2023-11-21 11:32 CodeNoobKingKc2 Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class StaticBatchInstallEventListenerTest {

    @InjectMocks
    private StaticBatchInstallEventListener arkletApplicationListener;

    @Mock
    private UnifiedOperationService         operationService;

    MockedStatic<ArkletComponentRegistry>   componentRegistryMockedStatic = null;

    @Before
    public void beforeTest() {
        componentRegistryMockedStatic = mockStatic(ArkletComponentRegistry.class);
        System.setProperty("com.alipay.sofa.ark.static.biz.dir", "/path/to/dir");
    }

    @After
    public void afterTest() {
        componentRegistryMockedStatic.close();
        System.clearProperty("com.alipay.sofa.ark.static.biz.dir");
    }

    @SneakyThrows
    @Test
    public void testBatchDeployFromLocalDir() {
        BatchInstallResponse response = null;
        ContextRefreshedEvent event = null;
        {
            componentRegistryMockedStatic.when(ArkletComponentRegistry::getOperationServiceInstance).thenReturn(operationService);

            response = BatchInstallResponse.builder().
                    code(ResponseCode.SUCCESS).
                    bizUrlToResponse(new HashMap<>()).
                    build();

            response.getBizUrlToResponse().put("foo", new ClientResponse());
            response.getBizUrlToResponse().get("foo").setCode(ResponseCode.SUCCESS);

            doReturn(response).when(operationService).batchInstall(BatchInstallRequest.builder().
                    bizDirAbsolutePath("/path/to/dir").
                    build());

            event = mock(ContextRefreshedEvent.class);
            doReturn(mock(ApplicationContext.class)).when(event).getApplicationContext();
        }

        arkletApplicationListener.onApplicationEvent(event);

        {
            verify(operationService, times(1)).batchInstall(any(BatchInstallRequest.class));
        }

    }

    @SneakyThrows
    @Test
    public void testBatchDeployFromLocalDir_Skip() {
        ContextRefreshedEvent event = null;
        {
            event = mock(ContextRefreshedEvent.class);
            ApplicationContext mockdcontext = mock(ApplicationContext.class);
            doReturn(mockdcontext).when(event).getApplicationContext();
            doReturn(mockdcontext).when(mockdcontext).getParent();
        }

        arkletApplicationListener.onApplicationEvent(event);

        {
            verify(operationService, never()).batchInstall(any(BatchInstallRequest.class));
        }

    }
}
