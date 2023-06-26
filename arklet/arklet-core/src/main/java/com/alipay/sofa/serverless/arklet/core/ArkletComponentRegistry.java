package com.alipay.sofa.serverless.arklet.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.sofa.serverless.arklet.core.api.ApiClient;
import com.alipay.sofa.serverless.arklet.core.command.CommandService;
import com.alipay.sofa.serverless.arklet.core.command.CommandServiceImpl;
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

    private static final List<ArkletComponent> componentList = new ArrayList<>(8);
    private final AtomicBoolean init = new AtomicBoolean(false);
    private final AtomicBoolean destroy = new AtomicBoolean(false);

    private static final Injector componentInjector;

    static {
        Injector injector = Guice.createInjector(new ComponentGuiceModule());
        componentInjector = injector;
        for (Binding<ArkletComponent> binding : injector
            .findBindingsByType(new TypeLiteral<ArkletComponent>() {
            })) {
            componentList.add(binding.getProvider().get());
        }
    }

    public ArkletComponentRegistry() {

    }

    public void initComponents() {
        if (init.compareAndSet(false, true)) {
            for (ArkletComponent component : componentList) {
                component.init();
            }
        }
    }

    public void destroyComponents() {
        if (destroy.compareAndSet(false, true)) {
            for (ArkletComponent component : componentList) {
                component.destroy();
            }
        }
    }

    public static UnifiedOperationService getOperationServiceInstance() {
        return componentInjector.getInstance(UnifiedOperationService.class);
    }

    public static CommandService getCommandServiceInstance() {
        return componentInjector.getInstance(CommandService.class);
    }

    public static ApiClient getApiClientInstance() {
        return componentInjector.getInstance(ApiClient.class);
    }

    //public static List<ArkletComponent> getAllComponents() {
    //    return componentList;
    //}



    private static class ComponentGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<ArkletComponent> componentMultibinder = Multibinder.newSetBinder(binder(),
                ArkletComponent.class);
            componentMultibinder.addBinding().to(CommandServiceImpl.class);
            componentMultibinder.addBinding().to(ApiClient.class);
            componentMultibinder.addBinding().to(UnifiedOperationServiceImpl.class);

            bind(CommandService.class).to(CommandServiceImpl.class);
            bind(UnifiedOperationService.class).to(UnifiedOperationServiceImpl.class);

        }
    }
}
