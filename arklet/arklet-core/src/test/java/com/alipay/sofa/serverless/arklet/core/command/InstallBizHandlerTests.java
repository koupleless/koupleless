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
import com.alipay.sofa.serverless.arklet.core.BaseTest;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author mingmen
 * @date 2023/9/5
 */
public class InstallBizHandlerTests extends BaseTest {

    ///**
    // * 测试用例编号：InstallBizHandlerTests001
    // * 测试方法：handle
    // * 测试目的：验证当获取到的ClientResponse的code为SUCCESS时，handle方法返回的Output是否为成功状态
    // * 测试数据：Input对象，其中bizUrl属性为任意非空字符串
    // * 预期结果：handle方法返回的Output对象为成功状态
    // */
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

    ///**
    // * 测试用例编号：InstallBizHandlerTests002
    // * 测试方法：handle
    // * 测试目的：验证当获取到的ClientResponse的code不为SUCCESS时，handle方法返回的Output是否为失败状态
    // * 测试数据：Input对象，其中bizUrl属性为任意非空字符串
    // * 预期结果：handle方法返回的Output对象为失败状态
    // */
    //@Test
    //public void testHandle_Failure() throws Throwable {
    //
    //    InstallBizHandler handler = (InstallBizHandler)commandService.getHandler(BuiltinCommand.INSTALL_BIZ);
    //
    //
    //    // 准备测试数据
    //    Input input = new Input();
    //    input.setBizUrl("testUrl");
    //
    //    ClientResponse response = new ClientResponse();
    //    response.setCode(ResponseCode.FAILED);
    //
    //    // 设置Mock行为
    //    when(operationService.install(anyString())).thenReturn(response);
    //
    //    // 执行测试
    //    Output<ClientResponse> result = handler.handle(input);
    //
    //    // 验证结果
    //    assertSame(response, result.getData());
    //    assertFalse(result.failed());
    //}

    /**
     * 测试用例编号：InstallBizHandlerTests003
     * 测试方法：validate
     * 测试目的：验证当input的bizName属性为空字符串时，validate方法会抛出CommandValidationException异常
     * 测试数据：Input对象，其中bizName属性为空字符串
     * 预期结果：validate方法抛出CommandValidationException异常
     */
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

    /**
     * 测试用例编号：InstallBizHandlerTests004
     * 测试方法：validate
     * 测试目的：验证当input的bizVersion属性为空字符串时，validate方法会抛出CommandValidationException异常
     * 测试数据：Input对象，其中bizVersion属性为空字符串
     * 预期结果：validate方法抛出CommandValidationException异常
     */
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

    /**
     * 测试用例编号：InstallBizHandlerTests005
     * 测试方法：validate
     * 测试目的：验证当input的isAync属性为true且requestId为空字符串时，validate方法会抛出CommandValidationException异常
     * 测试数据：Input对象，其中isAync属性为true，requestId属性为空字符串
     * 预期结果：validate方法抛出CommandValidationException异常
     */
    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankRequestId() throws CommandValidationException {

        InstallBizHandler handler = (InstallBizHandler) commandService
            .getHandler(BuiltinCommand.INSTALL_BIZ);

        // 准备测试数据
        Input input = new Input();
        input.setAync(true);
        input.setRequestId("");

        // 执行测试
        handler.validate(input);
    }

    /**
     * 测试用例编号：InstallBizHandlerTests006
     * 测试方法：validate
     * 测试目的：验证当input的bizUrl属性为空字符串时，validate方法会抛出CommandValidationException异常
     * 测试数据：Input对象，其中bizUrl属性为空字符串
     * 预期结果：validate方法抛出CommandValidationException异常
     */
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
