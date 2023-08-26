package com.alipay.sofa.serverless.arklet.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.alipay.sofa.ark.api.ArkClient;
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

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();
    private static final List<ArkletComponent> componentList = new ArrayList<>(8);
    private final AtomicBoolean init = new AtomicBoolean(false);
    private final AtomicBoolean destroy = new AtomicBoolean(false);

    private static final Injector componentInjector;

    static {
        componentInjector = Guice.createInjector(new ComponentGuiceModule());
        for (Binding<ArkletComponent> binding : componentInjector
            .findBindingsByType(new TypeLiteral<ArkletComponent>() {
            })) {
            componentList.add(binding.getProvider().get());
        }
    }

    public ArkletComponentRegistry() {

    }

    public void initComponents() {
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

    public void destroyComponents() {
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

            bind(CommandService.class).to(CommandServiceImpl.class);
            bind(UnifiedOperationService.class).to(UnifiedOperationServiceImpl.class);

        }
    }
}
