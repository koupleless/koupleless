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
package com.alipay.sofa.koupleless.arklet.core.command.custom;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.koupleless.arklet.core.util.AssertUtils;

/**
 * @author mingmen
 * @date 2023/8/6
 */
public class CustomCommandHandler extends AbstractCommandHandler<Input, String> {

    @Override
    public void validate(Input input) throws CommandValidationException {
        AssertUtils.isTrue(input.id > 0, "input id should larger than 0");
        AssertUtils.isTrue(!StringUtils.isEmpty(input.userName), "input name should not be emptu");
    }

    @Override
    public Output<String> handle(Input input) {
        return Output.ofSuccess("hello world");
    }

    @Override
    public Command command() {
        return CustomCommand.HELLO;
    }
}
