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
package com.alipay.sofa.koupleless.arklet.springboot.starter.command;

import java.util.Map;

import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author mingmen
 * @date 2023/6/14
 * custom directive extension for master base application
 */
@SuppressWarnings("rawtypes")
public class MasterBizCmdHandlerCollector implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractCommandHandler> map = applicationContext.getBeansOfType(AbstractCommandHandler.class);
        map.forEach((k, v) -> {
            // find custom directive beans from master base's spring context
            ArkletComponentRegistry.getCommandServiceInstance().registerCommandHandler(v);
        });
    }
}
