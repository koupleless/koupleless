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
package com.alipay.sofa.koupleless.arklet.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alipay.sofa.koupleless.arklet.core.health.HealthService;
import com.alipay.sofa.koupleless.arklet.core.health.HealthServiceImpl;
import com.alipay.sofa.koupleless.arklet.core.api.ApiClient;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.CommandServiceImpl;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationServiceImpl;
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

    private static final Injector        componentInjector = Guice
                                                               .createInjector(new ComponentGuiceModule());

    private static List<ArkletComponent> componentList     = new ArrayList<>(8);

    static {
        for (Binding<ArkletComponent> binding : componentInjector
            .findBindingsByType(new TypeLiteral<ArkletComponent>() {
            })) {
            componentList.add(binding.getProvider().get());
        }
        initComponents();
    }

    private static void initComponents() {
            String components = componentList.stream().map(s -> s.getClass().getSimpleName()).collect(Collectors.joining(", "));
            ArkletLoggerFactory.getDefaultLogger().info("found components: {}", components);
            ArkletLoggerFactory.getDefaultLogger().info("start to initialize components");
            for (ArkletComponent component : componentList) {
                component.init();
            }
            ArkletLoggerFactory.getDefaultLogger().info("finish initialize components");
    }

    private static void destroyComponents() {
        ArkletLoggerFactory.getDefaultLogger().info("start to destroy components");
        for (ArkletComponent component : componentList) {
            component.destroy();
        }
        ArkletLoggerFactory.getDefaultLogger().info("finish destroy components");
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
