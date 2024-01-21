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
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.InstallBizHandler;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.InstallBizHandler.Input;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.koupleless.arklet.core.health.custom.model.CustomBiz;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * @author mingmen
 * @date 2023/9/5
 */
public class InstallBizHandlerTest extends BaseHandlerTest {

    private InstallBizHandler handler;

    public BizFactoryService  bizFactoryService = Mockito.mock(BizFactoryService.class);

    @Before
    public void setupInstallBizHandler() {
        handler = (InstallBizHandler) commandService.getHandler(BuiltinCommand.INSTALL_BIZ);
    }

    @Test
    public void testHandle_Success() throws Throwable {
        Input input = new Input();
        input.setBizUrl("testUrl");

        when(handler.getOperationService().install(input.getBizUrl())).thenReturn(success);

        Output<InstallBizHandler.InstallBizClientResponse> result = handler.handle(input);

        Assert.assertEquals(success.getBizInfos(), result.getData().getBizInfos());
        Assert.assertEquals(success.getMessage(), result.getData().getMessage());
        Assert.assertEquals(success.getCode(), result.getData().getCode());
        Assert.assertTrue(result.success());
    }

    @Test
    public void testHandle_Failure() throws Throwable {
        Input input = new Input();
        input.setBizUrl("testUrl");

        when(handler.getOperationService().install(input.getBizUrl())).thenReturn(failed);

        Output<InstallBizHandler.InstallBizClientResponse> result = handler.handle(input);

        Assert.assertEquals(failed.getBizInfos(), result.getData().getBizInfos());
        Assert.assertEquals(failed.getMessage(), result.getData().getMessage());
        Assert.assertEquals(failed.getCode(), result.getData().getCode());
        Assert.assertTrue(result.failed());
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizName() throws CommandValidationException {
        // 准备测试数据
        Input input = new Input();
        input.setBizName("");

        // 执行测试
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizVersion() throws CommandValidationException {
        // 准备测试数据
        Input input = new Input();
        input.setBizVersion("");

        // 执行测试
        handler.validate(input);
    }

    @Test
    public void testValidate_BlankBizName_BlankBizVersion() throws CommandValidationException, IOException {
        // 准备测试数据
        URL url = this.getClass().getClassLoader().getResource("test-biz.jar");
        String bizName = "sofa-ark-sample-springboot-ark";
        String bizVersion = "0.3.0";

        Input input = new Input();
        input.setBizName("");
        input.setBizVersion("");
        input.setBizUrl("file://" + url.getFile());


        when(bizFactoryService.createBiz(any(File.class))).thenReturn(new CustomBiz(bizName, bizVersion));
        arkClient.when(ArkClient::getBizFactoryService).thenReturn(bizFactoryService);
        arkClient.when(() -> ArkClient.createBizSaveFile(anyString(), anyString())).thenReturn(new File(url.getFile()));

        // 执行测试
        handler.validate(input);
        Assert.assertEquals(bizName, input.getBizName());
        Assert.assertEquals(bizVersion, input.getBizVersion());
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BizName_BlankBizVersion() throws CommandValidationException,
                                                      IOException {
        // 准备测试数据
        URL url = this.getClass().getClassLoader().getResource("test-biz.jar");
        String bizName = "sofa-ark-sample-springboot-ark";
        String bizVersion = "0.3.0";

        Input input = new Input();
        input.setBizName(bizName);
        input.setBizVersion("");
        input.setBizUrl("file://" + url.getFile());

        // 执行测试
        handler.validate(input);
    }

    @Test
    public void testValidate_BizName_BizVersion() throws CommandValidationException {
        // 准备测试数据
        URL url = this.getClass().getClassLoader().getResource("test-biz.jar");
        String bizName = "sofa-ark-sample-springboot-ark";
        String bizVersion = "0.3.0";

        Input input = new Input();
        input.setBizName(bizName);
        input.setBizVersion(bizVersion);
        input.setBizUrl("file://" + url.getFile());

        // 执行测试
        handler.validate(input);
        // 测试验证 ArkClient.createBizSaveFile 被调用 0 次
        arkClient.verify(() -> ArkClient.createBizSaveFile(anyString(), anyString()), times(0));
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankRequestId() throws CommandValidationException {
        // 执行测试
        Input input = new Input();
        input.setAsync(true);
        input.setRequestId("");

        // 执行测试
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizUrl() throws CommandValidationException {
        // 准备测试数据
        Input input = new Input();
        input.setBizUrl("");

        // 执行测试
        handler.validate(input);
    }

}
