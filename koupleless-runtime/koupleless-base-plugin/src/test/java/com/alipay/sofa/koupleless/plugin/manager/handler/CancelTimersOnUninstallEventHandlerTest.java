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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.koupleless.plugin.manager.handler.CancelTimersOnUninstallEventHandler.manageTimer;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

/**
 * @author lylingzhen
 * @version CancelTimersOnUninstallEventHandlerTest.java
 */
public class CancelTimersOnUninstallEventHandlerTest {

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
    public void testManageTimers() {

        ClassLoader newClassLoader = new URLClassLoader(new URL[1],
            CancelTimersOnUninstallEventHandler.class.getClassLoader());
        BizModel bizModel = new BizModel();
        bizModel.setClassLoader(newClassLoader);
        BeforeBizRecycleEvent beforeBizRecycleEvent = new BeforeBizRecycleEvent(bizModel);
        new CancelTimersOnUninstallEventHandler().handleEvent(beforeBizRecycleEvent);

        final AtomicBoolean timer1Executed = new AtomicBoolean(false);
        Timer timer1 = new Timer();
        timer1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timer1Executed.set(true);
            }
        }, 1000, MAX_VALUE);
        manageTimer(timer1);

        currentThread().setContextClassLoader(newClassLoader);

        final AtomicBoolean timer2Executed = new AtomicBoolean(false);
        Timer timer2 = new Timer();
        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timer2Executed.set(true);
            }
        }, 1000, MAX_VALUE);
        manageTimer(timer2);

        assertEquals(false, timer1Executed.get());
        assertEquals(false, timer2Executed.get());

        new CancelTimersOnUninstallEventHandler().handleEvent(beforeBizRecycleEvent);

        try {
            sleep(1500);
        } catch (InterruptedException e) {
            // do nothing
        }

        assertEquals(true, timer1Executed.get());
        assertEquals(false, timer2Executed.get());
    }
}
