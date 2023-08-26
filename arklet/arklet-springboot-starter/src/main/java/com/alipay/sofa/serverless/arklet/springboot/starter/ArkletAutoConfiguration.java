package com.alipay.sofa.serverless.arklet.springboot.starter;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorRegistry;
import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.SpringBootUtil;
import com.alipay.sofa.serverless.arklet.springboot.starter.command.MasterBizCmdHandlerCollector;
import com.alipay.sofa.serverless.arklet.springboot.starter.environment.ConditionalOnMasterBiz;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@Configuration
public class ArkletAutoConfiguration {

    @Bean
    @ConditionalOnMasterBiz
    public ArkletComponentRegistry componentRegistry() {
        ArkletComponentRegistry registry = new ArkletComponentRegistry();
        registry.initComponents();
        return registry;
    }

    @Bean
    @ConditionalOnMasterBiz
    @DependsOn("componentRegistry")
    public MasterBizCmdHandlerCollector masterBizCmdHandlerCollector() {
        return new MasterBizCmdHandlerCollector();
    }

    @Bean
    @ConditionalOnMasterBiz
    public SpringBootUtil springBootUtil() {
        return new SpringBootUtil();
    }

    @Bean
    @ConditionalOnMasterBiz
    @DependsOn("springBootUtil")
    public ActuatorRegistry actuatorRegistry() {
        ActuatorRegistry registry = new ActuatorRegistry();
        registry.initActuator();
        return registry;
    }
}
