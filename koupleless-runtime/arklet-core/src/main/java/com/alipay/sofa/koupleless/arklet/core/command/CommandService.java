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

import java.util.List;
import java.util.Map;

import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponent;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@SuppressWarnings("rawtypes")
public interface CommandService extends ArkletComponent {

    /**
     * register command handler
     * @param handler handler
     */
    void registerCommandHandler(AbstractCommandHandler handler);

    /**
     * get command handler
     * @param command command
     * @return handler
     */
    AbstractCommandHandler getHandler(Command command);

    /**
     * get command handler
     * @param commandId commandId
     * @return handler
     */
    AbstractCommandHandler getHandler(String commandId);

    /**
     * core method
     * @param cmd command handler
     * @param content detail for this command
     * @return process result
     */
    Output<?> process(String cmd, Map content) throws InterruptedException;

    /**
     * if the cmd supported
     * @param cmd command handler
     * @return whether supported
     */
    boolean supported(String cmd);

    /**
     * get all handlers
     * @return handlers
     */
    List<AbstractCommandHandler> listAllHandlers();

}
