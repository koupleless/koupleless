package com.alipay.sofa.serverless.arklet.springboot.actuator;

import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.serverless.arklet.springboot.actuator.api.ActuatorClient;
import com.google.inject.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Lunarscave
 */
public class ActuatorRegistry {
    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    private final AtomicBoolean init = new AtomicBoolean(false);
    private final AtomicBoolean destroy = new AtomicBoolean(false);

    private static final Injector actuatorInjector;

    static {
        actuatorInjector = Guice.createInjector(new ActuatorGuiceModule());
    }

    public ActuatorRegistry() {

    }

    public void initActuator() {
        LOGGER.info("finish initialize actuator");
    }

    public static ActuatorClient getActuatorClient() {
        return actuatorInjector.getInstance(ActuatorClient.class);
    }

    private static class ActuatorGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }
}
