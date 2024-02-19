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

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author lylingzhen
 * @version ForceStopThreadsOnUninstallEventHandler.java
 *
 * 强制使用 thread.stop 关闭线程 (不推荐), 保险起见监听 AfterBizStopEvent 模块事件
 */
public class ForceStopThreadsOnUninstallEventHandler implements EventHandler<BeforeBizRecycleEvent> {

    private static final Logger                                                LOGGER                      = getLogger(ForceStopThreadsOnUninstallEventHandler.class);

    static final ConcurrentHashMap<ClassLoader, ConcurrentLinkedQueue<Thread>> BIZ_CLASS_LOADER_TO_THREADS = new ConcurrentHashMap<>();

    public static <T extends Thread> T manageThread(T thread) {
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        BIZ_CLASS_LOADER_TO_THREADS.putIfAbsent(contextClassLoader, new ConcurrentLinkedQueue<>());
        BIZ_CLASS_LOADER_TO_THREADS.get(contextClassLoader).add(thread);
        return thread;
    }

    @Override
    public void handleEvent(BeforeBizRecycleEvent event) {

        ClassLoader bizClassLoader = event.getSource().getBizClassLoader();
        LOGGER.info(
            "[ForceStopThreadsOnUninstallEventHandler] Module name: {} , BizClassLoader: {} .",
            event.getSource().getBizName(), bizClassLoader);

        ConcurrentLinkedQueue<Thread> threads = BIZ_CLASS_LOADER_TO_THREADS.get(bizClassLoader);
        if (threads == null) {
            LOGGER
                .info(
                    "[ForceStopThreadsOnUninstallEventHandler] No managed thread found for module: {} , just return. ",
                    event.getSource().getBizName());
            return;
        }

        LOGGER.info(
            "[ForceStopThreadsOnUninstallEventHandler] {} managed threads found for module: {} . ",
            threads.size(), event.getSource().getBizName());
        for (Thread thread : threads) {
            // 强制 stop 模块创建的线程,会导致线程持有的锁被强制释放,可能触发其它等待该锁的线程被异常唤醒继续执行,进而导致一些非预期行为,请谨慎使用
            thread.stop();
        }
    }

    @Override
    public int getPriority() {
        return LOWEST_PRECEDENCE;
    }
}
