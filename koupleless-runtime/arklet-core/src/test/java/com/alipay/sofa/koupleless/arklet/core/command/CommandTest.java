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
package com.alipay.sofa.koupleless.arklet.core.command;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.InstallBizHandler;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.QueryBizOpsHandler;
import com.alipay.sofa.koupleless.arklet.core.command.custom.CustomCommand;
import com.alipay.sofa.koupleless.arklet.core.command.custom.CustomCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mingmen
 * @date 2023/6/26
 */
public class CommandTest extends BaseTest {

    @Test
    public void registerCustomCommand() {
        commandService.registerCommandHandler(new CustomCommandHandler());
        CustomCommandHandler handler = (CustomCommandHandler) commandService
            .getHandler(CustomCommand.HELLO);
        Assert.assertNotNull(handler);
    }

    @Test
    public void commandProcess() throws Exception {
        Output output = commandService.process(BuiltinCommand.HELP.getId(), new HashMap());
        Assert.assertNotNull(output);
    }

    @Test
    public void testInstallHandler() throws InterruptedException {
        String rid = "testRequestId";

        InstallBizHandler.Input input = new InstallBizHandler.Input();
        input.setBizName("testBizName");
        input.setBizVersion("testBizVersion");
        input.setBizUrl("testBizUrl");
        Map map = JSONObject.parseObject(JSONObject.toJSONString(input), Map.class);
        try {
            commandService.process(BuiltinCommand.INSTALL_BIZ.getId(), map);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        input.setAsync(true);
        input.setRequestId(rid);
        Map map1 = JSONObject.parseObject(JSONObject.toJSONString(input), Map.class);
        Output<?> output1 = commandService.process(BuiltinCommand.INSTALL_BIZ.getId(), map1);
        Assert.assertNotNull(output1);
        ProcessRecord processRecord = (ProcessRecord) output1.getData();
        Assert.assertNotNull(processRecord);

        Thread.sleep(2000);

        QueryBizOpsHandler.Input input1 = new QueryBizOpsHandler.Input();
        input1.setRequestId(rid);
        Map map2 = JSONObject.parseObject(JSONObject.toJSONString(input1), Map.class);
        Output<?> output2 = commandService.process(BuiltinCommand.QUERY_BIZ_OPS.getId(), map2);
        Assert.assertNotNull(output2);
    }

}
