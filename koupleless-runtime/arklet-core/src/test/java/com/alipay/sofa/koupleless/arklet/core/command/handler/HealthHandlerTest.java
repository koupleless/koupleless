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

import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.HealthHandler;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.HealthHandler.Input;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lunarscave
 */
public class HealthHandlerTest extends BaseHandlerTest {

    private HealthHandler handler;

    @Before
    public void setupHealthHandlerTest() {
        handler = (HealthHandler) commandService.getHandler(BuiltinCommand.HEALTH);
    }

    private void testValidate(Input input) throws CommandValidationException {
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_InvalidType() throws CommandValidationException {
        Input input = new Input();
        input.setType("non type");
        testValidate(input);
    }

    @Test
    public void testCommand() {
        Command result = new HealthHandler().command();
        Assert.assertEquals(result, BuiltinCommand.HEALTH);
    }

    @Test
    public void testHandle_Success() {
        Output<Health> output = handler.handle(new Input());
        Assert.assertTrue(output.success());
    }

}
