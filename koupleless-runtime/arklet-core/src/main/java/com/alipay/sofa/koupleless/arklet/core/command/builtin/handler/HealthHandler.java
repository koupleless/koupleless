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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health.HealthBuilder;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Lunarscave
 */
public class HealthHandler extends AbstractCommandHandler<HealthHandler.Input, Health> {
    @Override
    public void validate(Input input) throws CommandValidationException {
        if (input != null) {
            String type = input.getType();
            if (!StringUtils.isEmpty(input.getType())) {
                isTrue(Constants.typeOfQuery(type), "type: %s can not be found", type);
            }
        }
    }

    @Override
    public Output<Health> handle(Input input) {

        input = input == null ? new Input() : input;
        String type = input.getType();
        HealthBuilder builder = new HealthBuilder();

        // set query strategy
        if (StringUtils.isEmpty(type)) {
            builder.putAllHealthData(getHealthService().queryMasterBiz());
        }
        if (StringUtils.isEmpty(type) || Constants.SYSTEM.equals(type)) {
            builder.putAllHealthData(getHealthService().getHealth(input.getMetrics()));
        }
        if (StringUtils.isEmpty(type) || Constants.typeOfInfo(type)) {
            String name = input.getModuleName();
            String version = input.getModuleVersion();
            builder.putAllHealthData(getHealthService().queryModuleInfo(type, name, version));
        }
        Health health = builder.build();

        if (health.containsError(Constants.HEALTH_ERROR)) {
            return Output.ofFailed(health.getHealthData().get(Constants.HEALTH_ERROR).toString());
        } else {
            return Output.ofSuccess(health);
        }
    }

    @Override
    public Command command() {
        return BuiltinCommand.HEALTH;
    }

    @Getter
    @Setter
    public static class Input extends ArkBizMeta {
        private String   type;
        private String   moduleName;
        private String   moduleVersion;
        private String[] metrics;
    }
}
