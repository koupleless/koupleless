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
package com.alipay.sofa.serverless.arklet.core.command;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.serverless.arklet.core.BaseTest;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.when;

/**
 * @author mingmen
 * @date 2023/9/5
 */
public class InstallBizHandlerTests extends BaseTest {

    //@Test
    //public void testHandle_Success() throws Throwable {
    //
    //    InstallBizHandler handler = (InstallBizHandler)commandService.getHandler(BuiltinCommand.INSTALL_BIZ);
    //
    //    // 准备测试数据
    //    Input input = new Input();
    //    input.setBizUrl("testUrl");
    //
    //    ClientResponse response = new ClientResponse();
    //    response.setCode(ResponseCode.SUCCESS);
    //
    //    // 设置Mock行为
    //    when(operationService.install(anyString())).thenReturn(response);
    //
    //    // 执行测试
    //    Output<ClientResponse> result = handler.handle(input);
    //
    //    // 验证结果
    //    assertSame(response, result.getData());
    //    assertTrue(result.success());
    //}

    //    @Test(expected = ArkRuntimeException.class)
    //    public void testHandle_Failure() throws Throwable {
    //
    //        InstallBizHandler handler = (InstallBizHandler) commandService
    //            .getHandler(BuiltinCommand.INSTALL_BIZ);
    //
    //        // 准备测试数据
    //        Input input = new Input();
    //        input.setBizUrl("testUrl");
    //
    //        ClientResponse response = new ClientResponse();
    //        response.setCode(ResponseCode.FAILED);
    //
    //        // 设置Mock行为
    //        when(operationService.install(input.getBizUrl())).thenReturn(response);
    //
    //        // 执行测试
    //        Output<ClientResponse> result = handler.handle(input);
    //
    //        // 验证结果
    //        Assert.assertSame(response, result.getData());
    //        Assert.assertTrue(result.failed());
    //    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizName() throws CommandValidationException {

        InstallBizHandler handler = (InstallBizHandler) commandService
            .getHandler(BuiltinCommand.INSTALL_BIZ);
        // 准备测试数据
        Input input = new Input();
        input.setBizName("");

        // 执行测试
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizVersion() throws CommandValidationException {

        InstallBizHandler handler = (InstallBizHandler) commandService
            .getHandler(BuiltinCommand.INSTALL_BIZ);
        // 准备测试数据
        Input input = new Input();
        input.setBizVersion("");

        // 执行测试
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankRequestId() throws CommandValidationException {

        InstallBizHandler handler = (InstallBizHandler) commandService
            .getHandler(BuiltinCommand.INSTALL_BIZ);

        // 准备测试数据
        Input input = new Input();
        input.setAsync(true);
        input.setRequestId("");

        // 执行测试
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizUrl() throws CommandValidationException {
        InstallBizHandler handler = (InstallBizHandler) commandService
            .getHandler(BuiltinCommand.INSTALL_BIZ);

        // 准备测试数据
        Input input = new Input();
        input.setBizUrl("");

        // 执行测试
        handler.validate(input);
    }
}
