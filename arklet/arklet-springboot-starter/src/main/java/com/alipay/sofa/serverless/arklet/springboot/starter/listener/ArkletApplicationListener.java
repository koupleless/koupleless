package com.alipay.sofa.serverless.arklet.springboot.starter.listener;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.command.CommandService;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.google.inject.Inject;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class ArkletApplicationListener implements ApplicationListener<ApplicationContextEvent> {

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    private CommandService commandService = ArkletComponentRegistry.getCommandServiceInstance();

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // 非基座应用直接跳过
        if (!Objects.equals(this.getClass().getClassLoader(), Thread.currentThread().getContextClassLoader()) || event.getApplicationContext().getParent() != null) {
            return;
        }
        if (event instanceof ContextRefreshedEvent) {
            List<AbstractCommandHandler> handlers = commandService.listAllHandlers();
            String commands = handlers.stream().map(s -> s.command().getId()).collect(Collectors.joining(", "));
            LOGGER.info("arklet supported commands:{}", commands);
        }
        if (event instanceof ContextClosedEvent) {
            ArkletComponentRegistry registry = event.getApplicationContext().getBean(ArkletComponentRegistry.class);
            registry.destroyComponents();
        }

    }
}
