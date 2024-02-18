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

import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.registry.ServiceFilter;
import com.alipay.sofa.ark.spi.registry.ServiceMetadata;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.koupleless.plugin.manager.handler.CancelTimersOnUninstallEventHandler;
import com.alipay.sofa.koupleless.plugin.manager.handler.ForceStopThreadsOnUninstallEventHandler;
import com.alipay.sofa.koupleless.plugin.manager.handler.ShutdownExecutorServicesOnUninstallEventHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServerlessRuntimeActivatorTest {

    private ServerlessRuntimeActivator serverlessRuntimeActivator = new ServerlessRuntimeActivator();

    @Test
    public void testStart() {

        List<EventHandler> eventhandlers = new ArrayList<>();

        EventAdminService eventAdminService = new EventAdminService() {
            @Override
            public void sendEvent(ArkEvent arkEvent) {

            }

            @Override
            public void register(EventHandler eventHandler) {
                eventhandlers.add(eventHandler);
            }

            @Override
            public void unRegister(EventHandler eventHandler) {

            }

            @Override
            public void unRegister(ClassLoader classLoader) {

            }
        };

        PluginContext pluginContext = new PluginContext() {
            @Override
            public Plugin getPlugin() {
                return null;
            }

            @Override
            public Plugin getPlugin(String s) {
                return null;
            }

            @Override
            public Set<String> getPluginNames() {
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public <T> ServiceReference<T> publishService(Class<T> aClass, T t) {
                return null;
            }

            @Override
            public <T> ServiceReference<T> publishService(Class<T> aClass, T t, String s) {
                return null;
            }

            @Override
            public <T> ServiceReference<T> referenceService(Class<T> aClass) {
                return (ServiceReference<T>) new ServiceReference<EventAdminService>() {
                    @Override
                    public int getPriority() {
                        return 0;
                    }

                    @Override
                    public EventAdminService getService() {
                        return eventAdminService;
                    }

                    @Override
                    public ServiceMetadata getServiceMetadata() {
                        return null;
                    }
                };
            }

            @Override
            public <T> ServiceReference<T> referenceService(Class<T> aClass, String s) {
                return null;
            }

            @Override
            public List<ServiceReference> referenceServices(ServiceFilter serviceFilter) {
                return null;
            }
        };

        serverlessRuntimeActivator.start(pluginContext);
        assertEquals(5, eventhandlers.size());
        assertEquals(ShutdownExecutorServicesOnUninstallEventHandler.class, eventhandlers.get(2)
            .getClass());
        assertEquals(CancelTimersOnUninstallEventHandler.class, eventhandlers.get(3).getClass());
        assertEquals(ForceStopThreadsOnUninstallEventHandler.class, eventhandlers.get(4).getClass());
    }

    @Test
    public void testRegisterCount() {

        PluginContext pluginContext = mock(PluginContext.class);
        EventAdminService eventAdminService = mock(EventAdminService.class);
        ServiceReferenceImpl<EventAdminService> impl = new ServiceReferenceImpl<>(eventAdminService);
        when(pluginContext.referenceService(EventAdminService.class)).thenReturn((impl));

        serverlessRuntimeActivator.start(pluginContext);
        verify(eventAdminService, times(5)).register(any());
    }

    @Test
    public void testStop() {
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
