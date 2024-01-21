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
package com.alipay.sofa.koupleless.arklet.core.command.builtin.handler;

import java.util.ArrayList;
import java.util.List;

import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.CommandModel;
import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;

/**
 * @author mingmen
 * @date 2023/6/14
 */

@SuppressWarnings("rawtypes")
public class HelpHandler extends AbstractCommandHandler<InputMeta, List<CommandModel>> {

    @Override
    public Output<List<CommandModel>> handle(InputMeta inputMeta) {
        List<AbstractCommandHandler> list = getCommandService().listAllHandlers();
        List<CommandModel> models = new ArrayList<>(list.size());
        for (AbstractCommandHandler handler : list) {
            models.add(new CommandModel(handler.command().getId(), handler.command().getDesc()));
        }
        return Output.ofSuccess(models);
    }

    @Override
    public Command command() {
        return BuiltinCommand.HELP;
    }

    @Override
    public void validate(InputMeta input) throws CommandValidationException {

    }

}
