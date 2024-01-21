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
package com.alipay.sofa.koupleless.arklet.core.command.handler;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.InstallBizHandler;
import com.alipay.sofa.koupleless.arklet.core.health.custom.CustomBizManagerService;
import com.alipay.sofa.koupleless.arklet.core.health.custom.CustomPluginManagerService;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

/**
 * @author lunarscave
 */
public class BaseHandlerTest extends BaseTest {

    public final InstallBizHandler.InstallBizClientResponse success = new InstallBizHandler.InstallBizClientResponse();
    public final InstallBizHandler.InstallBizClientResponse failed  = new InstallBizHandler.InstallBizClientResponse();
    public MockedStatic<ArkClient>                          arkClient;

    @Before
    public void setupHandler() {
        success.setCode(ResponseCode.SUCCESS);
        failed.setCode(ResponseCode.FAILED);

        arkClient = mockStatic(ArkClient.class);
        arkClient.when(() -> {
            ArkClient.installOperation(new BizOperation());
            ArkClient.uninstallBiz(anyString(), anyString());
        }).thenReturn(success);
        arkClient.when(ArkClient::getBizManagerService).thenReturn(new CustomBizManagerService());
        arkClient.when(ArkClient::getPluginManagerService).thenReturn(new CustomPluginManagerService());
        arkClient.when(ArkClient::getMasterBiz).thenReturn(new CustomBizManagerService().getMasterBiz());
    }

    @After
    public void tearDown() {
        arkClient.close();
    }

}
