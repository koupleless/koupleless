package com.alipay.sofa.serverless.arklet.springboot.starter.command;

import java.util.Map;

import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@SuppressWarnings("rawtypes")
public class MasterBizCmdHandlerCollector implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractCommandHandler> map = applicationContext.getBeansOfType(AbstractCommandHandler.class);
        map.forEach((k, v) -> {
            ArkletComponentRegistry.getCommandServiceInstance().registerCommandHandler(v);
        });
    }
}
