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
package com.alipay.sofa.koupleless.arklet.core.command.meta;

import java.lang.reflect.ParameterizedType;

import com.alipay.sofa.common.utils.StringUtil;
import com.alipay.sofa.koupleless.arklet.core.health.HealthService;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;

/**
 * @author mingmen
 * @date 2023/6/8
 */

@SuppressWarnings("unchecked")
public abstract class AbstractCommandHandler<P extends InputMeta, Q> {

    private final UnifiedOperationService unifiedOperationService = ArkletComponentRegistry
                                                                      .getOperationServiceInstance();
    private final CommandService          commandService          = ArkletComponentRegistry
                                                                      .getCommandServiceInstance();
    private final HealthService           healthService           = ArkletComponentRegistry
                                                                      .getHealthServiceInstance();

    public abstract void validate(P p) throws CommandValidationException;

    public abstract Output<Q> handle(P p);

    public abstract Command command();

    public UnifiedOperationService getOperationService() {
        return unifiedOperationService;
    }

    public CommandService getCommandService() {
        return commandService;
    }

    public HealthService getHealthService() {
        return healthService;
    }

    public Class<P> getInputClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<P>) parameterizedType.getActualTypeArguments()[0];
    }

    public static void isTrue(final boolean expression, final String message,
                              final Object... values) {
        if (!expression) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

    public static void notBlank(final String check, final String message, final Object... values) {
        if (StringUtil.isBlank(check)) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

    public static void notNull(final Object check, final String message, final Object... values) {
        if (null == check) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

}
