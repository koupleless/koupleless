package com.alipay.sofa.serverless.arklet.springboot.actuator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.api.ActuatorClient;
import com.alipay.sofa.serverless.arklet.springboot.actuator.common.log.ActuatorLogger;
import com.alipay.sofa.serverless.arklet.springboot.actuator.common.log.ActuatorLoggerFactory;
import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.SpringBootUtil;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.HealthActuatorService;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.HealthActuatorServiceImpl;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.MoudleInfoService;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.MoudleInfoServiceImpl;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties.Exposure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Lunarscave
 */
public class ActuatorRegistry {
    private static final ActuatorLogger LOGGER = ActuatorLoggerFactory.getDefaultLogger();
    private static final List<ActuatorComponent> componentList = new ArrayList<>();
    private final AtomicBoolean init = new AtomicBoolean(false);
    private final AtomicBoolean destroy = new AtomicBoolean(false);

    private WebEndpointProperties webEndpointProperties;

    private static final Injector actuatorInjector;

    static {
        actuatorInjector = Guice.createInjector(new ActuatorGuiceModule());
        for (Binding<ActuatorComponent> binding : actuatorInjector
                .findBindingsByType(new TypeLiteral<ActuatorComponent>() {
                })) {
            componentList.add(binding.getProvider().get());
        }
    }

    public ActuatorRegistry() {

    }

    public void initActuator() {
        if (init.compareAndSet(false, true)) {
            String components = componentList.stream().map(s -> s.getClass().getSimpleName()).collect(Collectors.joining(", "));
            LOGGER.info("found components: {}", components);
            LOGGER.info("start to initialize actuator");
            for (ActuatorComponent component : componentList) {
                component.init();
            }
            LOGGER.info("start to initialize actuator client");
            ActuatorClient.setMoudleInfoService(getMoudleInfoService());
            ActuatorClient.setHealthActuatorService(getHealthActuatorService());
            LOGGER.info("start to initalize acruator endpoint");
            initWebEndPoint();
            LOGGER.info("finish initialize actuator");
        }
    }

    public void destroyActuator() {
        if (destroy.compareAndSet(false, true)) {
            LOGGER.info("start to destroy actuator");
            for (ActuatorComponent component : componentList) {
                component.destroy();
            }
            LOGGER.info("finish destroy actuator");
        }
    }

    public static ActuatorClient getActuatorClient() {
        return actuatorInjector.getInstance(ActuatorClient.class);
    }

    public static MoudleInfoService getMoudleInfoService() {
        return actuatorInjector.getInstance(MoudleInfoServiceImpl.class);
    }

    public static HealthActuatorService getHealthActuatorService() {
        return actuatorInjector.getInstance(HealthActuatorServiceImpl.class);
    }

    private void initWebEndPoint() {
        this.webEndpointProperties = SpringBootUtil.getBean(WebEndpointProperties.class);
        Exposure exposure = this.webEndpointProperties.getExposure();
        Set<String> includePath = exposure.getInclude();
        includePath.add("*");
        this.webEndpointProperties.getExposure().setInclude(includePath);
        webEndpointProperties.setBasePath("/");
    }

    private static class ActuatorGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<ActuatorComponent> multibinder = Multibinder.newSetBinder(binder(),
                    ActuatorComponent.class);
            multibinder.addBinding().to(ActuatorClient.class);
            multibinder.addBinding().to(MoudleInfoServiceImpl.class);
            multibinder.addBinding().to(HealthActuatorServiceImpl.class);

            bind(MoudleInfoService.class).to(MoudleInfoServiceImpl.class);
            bind(HealthActuatorService.class).to(HealthActuatorServiceImpl.class);
        }
    }
}
