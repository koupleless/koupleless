package com.alipay.sofa.serverless.arklet.springboot.starter;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
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
        return new ArkletComponentRegistry();
    }

    @Bean
    @ConditionalOnMasterBiz
    @DependsOn("componentRegistry")
    public MasterBizCmdHandlerCollector masterBizCmdHandlerCollector() {
        return new MasterBizCmdHandlerCollector();
    }


}
