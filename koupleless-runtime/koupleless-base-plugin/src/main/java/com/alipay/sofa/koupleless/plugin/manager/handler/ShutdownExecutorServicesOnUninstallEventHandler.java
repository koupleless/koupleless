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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author lylingzhen
 * @version ShutdownExecutorServicesOnUninstallEventHandler.java
 *
 * 使用 executorService.shutdownNow 尝试优雅关闭所有托管的 ExecutorService (包括线程池), 因此监听 BeforeBizStopEvent 模块事件即可
 */
public class ShutdownExecutorServicesOnUninstallEventHandler implements
                                                            EventHandler<BeforeBizRecycleEvent> {

    private static final Logger                                                         LOGGER                                = getLogger(ShutdownExecutorServicesOnUninstallEventHandler.class);

    public static final String                                                          EXECUTOR_CLEANUP_TIMEOUT_SECONDS      = "com.alipay.koupleless.executor.cleanup.timeout.seconds";

    static final ConcurrentHashMap<ClassLoader, ConcurrentLinkedQueue<ExecutorService>> BIZ_CLASS_LOADER_TO_EXECUTOR_SERVICES = new ConcurrentHashMap<>();

    public static <T extends ExecutorService> T manageExecutorService(T actualExecutorService) {
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        BIZ_CLASS_LOADER_TO_EXECUTOR_SERVICES.putIfAbsent(contextClassLoader,
            new ConcurrentLinkedQueue<>());
        BIZ_CLASS_LOADER_TO_EXECUTOR_SERVICES.get(contextClassLoader).add(actualExecutorService);
        return actualExecutorService;
    }

    @Override
    public void handleEvent(BeforeBizRecycleEvent event) {

        ClassLoader bizClassLoader = event.getSource().getBizClassLoader();
        LOGGER
            .info(
                "[ShutdownExecutorServicesOnUninstallEventHandler] Module name: {} , BizClassLoader: {} .",
                event.getSource().getBizName(), bizClassLoader);

        ConcurrentLinkedQueue<ExecutorService> executorServices = BIZ_CLASS_LOADER_TO_EXECUTOR_SERVICES
            .get(bizClassLoader);
        if (executorServices == null) {
            LOGGER
                .info(
                    "[ShutdownExecutorServicesOnUninstallEventHandler] No managed executor service for module: {} , just return. ",
                    event.getSource().getBizName());
            return;
        }

        LOGGER
            .info(
                "[ShutdownExecutorServicesOnUninstallEventHandler] {} managed executor services found for module: {} . ",
                executorServices.size(), event.getSource().getBizName());

        int cleanupTimeoutSeconds = parseInt(getProperty(EXECUTOR_CLEANUP_TIMEOUT_SECONDS, "0"));
        for (ExecutorService executorService : executorServices) {
            // 强制 stop 模块创建的线程,会导致线程持有的锁被强制释放,可能触发其它等待该锁的线程被异常唤醒继续执行,进而导致一些非预期行为,
            // 因此不会强制调用 thread.stop 方法,只会尝试优雅异步关闭线程池
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(cleanupTimeoutSeconds, SECONDS);
            } catch (InterruptedException ie) {
                // 忽略 InterruptedException
                LOGGER
                    .warn(
                        "InterruptedException occurred while invoke executorService.awaitTermination. ",
                        ie);
            }
        }
    }

    @Override
    public int getPriority() {
        return HIGHEST_PRECEDENCE + 1;
    }
}
