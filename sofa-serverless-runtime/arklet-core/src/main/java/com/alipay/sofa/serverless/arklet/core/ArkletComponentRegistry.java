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
package com.alipay.sofa.serverless.arklet.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.alipay.sofa.serverless.arklet.core.health.HealthService;
import com.alipay.sofa.serverless.arklet.core.health.HealthServiceImpl;
import com.alipay.sofa.serverless.arklet.core.api.ApiClient;
import com.alipay.sofa.serverless.arklet.core.command.CommandService;
import com.alipay.sofa.serverless.arklet.core.command.CommandServiceImpl;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.serverless.arklet.core.ops.UnifiedOperationService;
import com.alipay.sofa.serverless.arklet.core.ops.UnifiedOperationServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class ArkletComponentRegistry {

    private static final ArkletLogger LOGGER            = ArkletLoggerFactory.getDefaultLogger();

    private static final Injector     componentInjector = Guice
                                                            .createInjector(new ComponentGuiceModule());

    private List<ArkletComponent>     componentList     = new ArrayList<>(8);
    private final AtomicBoolean       init              = new AtomicBoolean(false);
    private final AtomicBoolean       destroy           = new AtomicBoolean(false);

    public ArkletComponentRegistry() {
        for (Binding<ArkletComponent> binding : componentInjector
            .findBindingsByType(new TypeLiteral<ArkletComponent>() {
            })) {
            componentList.add(binding.getProvider().get());
        }
    }

    public synchronized void initComponents() {
        if (init.compareAndSet(false, true)) {
            String components = componentList.stream().map(s -> s.getClass().getSimpleName()).collect(Collectors.joining(", "));
            LOGGER.info("found components: {}", components);
            LOGGER.info("start to initialize components");
            for (ArkletComponent component : componentList) {
                component.init();
            }
            LOGGER.info("finish initialize components");
        }
    }

    public synchronized void destroyComponents() {
        if (destroy.compareAndSet(false, true)) {
            LOGGER.info("start to destroy components");
            for (ArkletComponent component : componentList) {
                component.destroy();
            }
            LOGGER.info("finish destroy components");
        }
    }

    public static UnifiedOperationService getOperationServiceInstance() {
        return componentInjector.getInstance(UnifiedOperationService.class);
    }

    public static CommandService getCommandServiceInstance() {
        return componentInjector.getInstance(CommandService.class);
    }

    public static HealthService getHealthServiceInstance() {
        return componentInjector.getInstance(HealthService.class);
    }

    public static ApiClient getApiClientInstance() {
        return componentInjector.getInstance(ApiClient.class);
    }

    private static class ComponentGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<ArkletComponent> componentMultibinder = Multibinder.newSetBinder(binder(),
                ArkletComponent.class);
            componentMultibinder.addBinding().to(CommandServiceImpl.class);
            componentMultibinder.addBinding().to(ApiClient.class);
            componentMultibinder.addBinding().to(UnifiedOperationServiceImpl.class);
            componentMultibinder.addBinding().to(HealthServiceImpl.class);

            bind(CommandService.class).to(CommandServiceImpl.class);
            bind(UnifiedOperationService.class).to(UnifiedOperationServiceImpl.class);
            bind(HealthService.class).to(HealthServiceImpl.class);
        }
    }
}
