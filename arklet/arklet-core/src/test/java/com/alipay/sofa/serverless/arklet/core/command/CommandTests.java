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

import java.util.HashMap;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author mingmen
 * @date 2023/6/26
 */
public class CommandTests {

    private static CommandService commandService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        if (commandService == null) {
            ArkletComponentRegistry registry = new ArkletComponentRegistry();
            registry.initComponents();
            commandService = ArkletComponentRegistry.getCommandServiceInstance();
        }
    }

    @Test
    public void registerCustomCommand() {
        commandService.registerCommandHandler(new CustomCommandHandler());
        CustomCommandHandler handler = (CustomCommandHandler) commandService
            .getHandler(CustomCommand.HELLO);
        Assert.assertNotNull(handler);
    }

    @Test
    public void process() throws Exception {
        Output output = commandService.process(BuiltinCommand.HELP.getId(), new HashMap());
        Assert.assertNotNull(output);
    }

}
