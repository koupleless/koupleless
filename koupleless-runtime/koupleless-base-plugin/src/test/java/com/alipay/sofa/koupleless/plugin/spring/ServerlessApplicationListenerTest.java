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
package com.alipay.sofa.koupleless.plugin.spring;

import org.junit.Test;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;

import static org.mockito.Mockito.mock;

/**
 * @author: yuanyuan
 * @date: 2023/11/2 9:39 下午
 */
public class ServerlessApplicationListenerTest {

    @Test
    public void testOnApplicationEvent() {
        ServerlessApplicationListener listener = new ServerlessApplicationListener();
        ApplicationStartingEvent applicationStartingEvent = new ApplicationStartingEvent(
            mock(ConfigurableBootstrapContext.class), mock(SpringApplication.class), new String[0]);
        listener.onApplicationEvent(applicationStartingEvent);
    }
}
