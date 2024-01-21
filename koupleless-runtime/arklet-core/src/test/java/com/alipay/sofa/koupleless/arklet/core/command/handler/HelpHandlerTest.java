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

import java.util.List;

import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.HelpHandler;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.CommandModel;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mingmen
 * @date 2023/9/6
 */
public class HelpHandlerTest extends BaseHandlerTest {

    private HelpHandler helpHandler;

    @Before
    public void setUp() {
        helpHandler = (HelpHandler) commandService.getHandler(BuiltinCommand.HELP);
    }

    @Test
    public void testHandle() {
        Output<List<CommandModel>> result = helpHandler.handle(new InputMeta());
        Assert.assertTrue(result.getData() != null && !result.getData().isEmpty());
    }

    @Test
    public void testCommand() {
        // Act
        Command result = helpHandler.command();

        // Assert
        assert result == BuiltinCommand.HELP;
    }

    @Test
    public void testValidate() {
        // Arrange
        InputMeta input = new InputMeta();

        // Act
        try {
            helpHandler.validate(input);
        } catch (CommandValidationException e) {
            assert false;
        }
    }

}
