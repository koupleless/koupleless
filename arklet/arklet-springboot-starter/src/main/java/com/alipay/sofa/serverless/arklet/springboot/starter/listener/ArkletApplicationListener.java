package com.alipay.sofa.serverless.arklet.springboot.starter.listener;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.command.CommandService;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorRegistry;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@SuppressWarnings("rawtypes")
public class ArkletApplicationListener implements ApplicationListener<ApplicationContextEvent> {

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    private final CommandService commandService = ArkletComponentRegistry.getCommandServiceInstance();

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // 非基座应用直接跳过
        if (!Objects.equals(this.getClass().getClassLoader(), Thread.currentThread().getContextClassLoader()) || event.getApplicationContext().getParent() != null) {
            return;
        }
        if (event instanceof ContextRefreshedEvent) {
            List<AbstractCommandHandler> handlers = commandService.listAllHandlers();
            String commands = handlers.stream().map(s -> s.command().getId()).collect(Collectors.joining(", "));
            LOGGER.info("total supported commands:{}", commands);
        }
        if (event instanceof ContextClosedEvent) {
            ArkletComponentRegistry componentRegistry = event.getApplicationContext().getBean(ArkletComponentRegistry.class);
            ActuatorRegistry actuatorRegistry = event.getApplicationContext().getBean(ActuatorRegistry.class);
            componentRegistry.destroyComponents();
            actuatorRegistry.destroyActuator();

        }

    }
}
