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

import static com.alipay.sofa.koupleless.plugin.manager.handler.ForceStopThreadsOnUninstallEventHandler.manageThread;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;

/**
 * @author lylingzhen
 * @version ForceStopThreadsOnUninstallEventHandlerTest.java
 */
public class ForceStopThreadsOnUninstallEventHandlerTest {

    private ClassLoader originalClassLoader = currentThread().getContextClassLoader();

    @Before
    public void setUp() {
        currentThread().setContextClassLoader(originalClassLoader);
    }

    @After
    public void tearDown() {
        currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void testManageThreads() {

        ClassLoader newClassLoader = new URLClassLoader(new URL[1], ForceStopThreadsOnUninstallEventHandler.class.getClassLoader());
        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(newClassLoader);
        BeforeBizRecycleEvent beforeBizRecycleEvent = new BeforeBizRecycleEvent(bizModel);
        new ForceStopThreadsOnUninstallEventHandler().handleEvent(beforeBizRecycleEvent);

        Object thread1Lock = new Object();
        Thread thread1 = manageThread(new Thread(() -> {
            synchronized (thread1Lock) {
                try {
                    thread1Lock.wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }));
        thread1.start();

        currentThread().setContextClassLoader(newClassLoader);

        Object thread2Lock = new Object();
        Thread thread2 = manageThread(new Thread(() -> {
            synchronized (thread2Lock) {
                try {
                    thread2Lock.wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }));
        thread2.start();

        assertEquals(true, thread1.isAlive());
        assertEquals(true, thread2.isAlive());

        new ForceStopThreadsOnUninstallEventHandler().handleEvent(beforeBizRecycleEvent);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {

        }

        assertEquals(true, thread1.isAlive());
        assertEquals(false, thread2.isAlive());
    }
}
