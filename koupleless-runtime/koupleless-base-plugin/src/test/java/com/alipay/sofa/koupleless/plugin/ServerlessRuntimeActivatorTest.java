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
package com.alipay.sofa.koupleless.plugin;

import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.registry.ServiceMetadata;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServerlessRuntimeActivatorTest {
    private ServerlessRuntimeActivator serverlessRuntimeActivator = new ServerlessRuntimeActivator();

    @Test
    public void start() {
        PluginContext pluginContext = mock(PluginContext.class);
        EventAdminService eventAdminService = mock(EventAdminService.class);
        ServiceReferenceImpl<EventAdminService> impl = new ServiceReferenceImpl<>(eventAdminService);
        when(pluginContext.referenceService(EventAdminService.class)).thenReturn((impl));

        serverlessRuntimeActivator.start(pluginContext);
        verify(eventAdminService, times(2)).register(any());
    }

    @Test
    public void stop() {
    }

    class ServiceReferenceImpl<T> implements ServiceReference<T> {
        private T t;

        ServiceReferenceImpl(T t) {
            this.t = t;
        }

        @Override
        public T getService() {
            return t;
        }

        @Override
        public ServiceMetadata getServiceMetadata() {
            return null;
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }
}
