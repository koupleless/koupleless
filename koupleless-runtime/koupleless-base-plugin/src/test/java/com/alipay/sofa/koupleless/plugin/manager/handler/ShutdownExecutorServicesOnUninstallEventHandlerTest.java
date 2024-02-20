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

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import static com.alipay.sofa.koupleless.plugin.manager.handler.ShutdownExecutorServicesOnUninstallEventHandler.EXECUTOR_CLEANUP_TIMEOUT_SECONDS;
import static com.alipay.sofa.koupleless.plugin.manager.handler.ShutdownExecutorServicesOnUninstallEventHandler.manageExecutorService;
import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * @author lylingzhen
 * @version ShutdownExecutorServicesOnUninstallEventHandlerTest.java
 */
public class ShutdownExecutorServicesOnUninstallEventHandlerTest {

    private ClassLoader originalClassLoader = currentThread().getContextClassLoader();

    @Before
    public void setUp() {
        clearProperty(EXECUTOR_CLEANUP_TIMEOUT_SECONDS);
        currentThread().setContextClassLoader(originalClassLoader);
    }

    @After
    public void tearDown() {
        currentThread().setContextClassLoader(originalClassLoader);
        clearProperty(EXECUTOR_CLEANUP_TIMEOUT_SECONDS);
    }

    @Test
    public void testManageExecutorServicesWithoutTimeout() {

        ExecutorService executorService1 = manageExecutorService(new ThreadPoolExecutor(10, 10, 10,
            MILLISECONDS, new ArrayBlockingQueue<>(10)));
        ExecutorService executorService2 = manageExecutorService(new ScheduledThreadPoolExecutor(10));

        ClassLoader newClassLoader = new URLClassLoader(new URL[1],
            ShutdownExecutorServicesOnUninstallEventHandler.class.getClassLoader());
        currentThread().setContextClassLoader(newClassLoader);

        ExecutorService executorService3 = manageExecutorService(new ThreadPoolExecutor(20, 20, 20,
            MILLISECONDS, new ArrayBlockingQueue<>(10)));

        assertEquals(false, executorService1.isShutdown());
        assertEquals(false, executorService2.isShutdown());
        assertEquals(false, executorService3.isShutdown());

        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(originalClassLoader);
        BeforeBizRecycleEvent beforeBizRecycleEvent = new BeforeBizRecycleEvent(bizModel);
        new ShutdownExecutorServicesOnUninstallEventHandler().handleEvent(beforeBizRecycleEvent);

        assertEquals(true, executorService1.isShutdown());
        assertEquals(true, executorService2.isShutdown());
        assertEquals(false, executorService3.isShutdown());
    }

    @Test
    public void testManageExecutorServicesWithTimeout() {

        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(originalClassLoader);
        BeforeBizRecycleEvent beforeBizRecycleEvent = new BeforeBizRecycleEvent(bizModel);
        new ShutdownExecutorServicesOnUninstallEventHandler().handleEvent(beforeBizRecycleEvent);

        ThreadPoolExecutor executorService = manageExecutorService(new ThreadPoolExecutor(10, 10, 10,
                MILLISECONDS, new ArrayBlockingQueue<>(10)));

        executorService.submit(() -> {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                // do nothing
            }
        });

        setProperty(EXECUTOR_CLEANUP_TIMEOUT_SECONDS, "2");
        testManageExecutorServicesWithoutTimeout();
        assertEquals(true, executorService.isShutdown());
        // 线程 sleep 经过 shutdown 会被中断退出:
        assertEquals(0, executorService.getActiveCount());
    }
}