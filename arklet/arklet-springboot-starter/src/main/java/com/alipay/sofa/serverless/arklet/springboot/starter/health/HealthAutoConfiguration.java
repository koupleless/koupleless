package com.alipay.sofa.serverless.arklet.springboot.starter.health;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint.ArkHealthCodeEndpoint;
import com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint.ArkHealthzEndpoint;
import com.alipay.sofa.serverless.arklet.springboot.starter.health.extension.indicator.MasterBizHealthIndicator;
import com.alipay.sofa.serverless.arklet.springboot.starter.environment.ConditionalOnMasterBiz;
import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Set;

/**
 * @author Lunarscave
 */
@Configuration
@ConditionalOnMasterBiz
public class HealthAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext context;

    @Bean
    public void initEndpoint() {
        WebEndpointProperties webEndpointProperties = this.context.getBean(WebEndpointProperties.class);
        WebEndpointProperties.Exposure exposure = webEndpointProperties.getExposure();
        Set<String> includePath = exposure.getInclude();
        includePath.add("*");
        webEndpointProperties.getExposure().setInclude(includePath);
        webEndpointProperties.setBasePath("/");
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public ArkHealthzEndpoint arkHealthzEndpoint() {
        return new ArkHealthzEndpoint();
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public ArkHealthCodeEndpoint arkHealthCodeEndpoint() {
        return new ArkHealthCodeEndpoint();
    }

    @Bean
    @DependsOn("componentRegistry")
    public MasterBizHealthIndicator masterBizHealthIndicator() {
        MasterBizHealthIndicator masterBizHealthIndicator = new MasterBizHealthIndicator();
        masterBizHealthIndicator.setApplicationAvailability(context.getBean(ApplicationAvailability.class));
        ArkletComponentRegistry.getHealthServiceInstance().registerIndicator(masterBizHealthIndicator);
        return masterBizHealthIndicator;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
