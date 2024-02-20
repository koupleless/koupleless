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
package com.alipay.sofa.koupleless.plugin.manager.handler;

import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.slf4j.Logger;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author lylingzhen
 * @version CancelTimersOnUninstallEventHandler.java
 *
 * 使用 timer.cancel 尝试优雅关闭所有托管的 Timer, 因此监听 BeforeBizStopEvent 模块事件即可
 */
public class CancelTimersOnUninstallEventHandler implements EventHandler<BeforeBizRecycleEvent> {

    private static final Logger                                               LOGGER                     = getLogger(CancelTimersOnUninstallEventHandler.class);

    static final ConcurrentHashMap<ClassLoader, ConcurrentLinkedQueue<Timer>> BIZ_CLASS_LOADER_TO_TIMERS = new ConcurrentHashMap<>();

    public static <T extends Timer> T manageTimer(T timer) {
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        BIZ_CLASS_LOADER_TO_TIMERS.putIfAbsent(contextClassLoader, new ConcurrentLinkedQueue<>());
        BIZ_CLASS_LOADER_TO_TIMERS.get(contextClassLoader).add(timer);
        return timer;
    }

    @Override
    public void handleEvent(BeforeBizRecycleEvent event) {

        ClassLoader bizClassLoader = event.getSource().getBizClassLoader();
        LOGGER.info("[CancelTimersOnUninstallEventHandler] Module name: {} , BizClassLoader: {} .",
            event.getSource().getBizName(), bizClassLoader);

        ConcurrentLinkedQueue<Timer> timers = BIZ_CLASS_LOADER_TO_TIMERS.get(bizClassLoader);
        if (timers == null) {
            LOGGER
                .info(
                    "[CancelTimersOnUninstallEventHandler] No managed timer found for module: {} , just return. ",
                    event.getSource().getBizName());
            return;
        }

        LOGGER.info(
            "[CancelTimersOnUninstallEventHandler] {} managed timers found for module: {} . ",
            timers.size(), event.getSource().getBizName());
        for (Timer timer : timers) {
            // 尝试优雅异步关闭 timer
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    public int getPriority() {
        return HIGHEST_PRECEDENCE;
    }
}
