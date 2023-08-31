package com.alipay.sofa.serverless.arklet.springboot.starter.actuator;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.ArkletHealthEndpointServiceImpl;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.health.ArkletHealthCodeEndpoint;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.health.ArkletHealthDetailsEndpoint;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.extension.indicator.MasterBizHealthIndicator;
import com.alipay.sofa.serverless.arklet.springboot.starter.environment.ConditionalOnMasterBiz;
import org.springframework.beans.BeansException;
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
public class ActuatorAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext context;

    @Bean
    @ConditionalOnMasterBiz
    public ArkletHealthDetailsEndpoint arkletHealthEndpoint() {
        WebEndpointProperties webEndpointProperties = this.context.getBean(WebEndpointProperties.class);
        WebEndpointProperties.Exposure exposure = webEndpointProperties.getExposure();
        Set<String> includePath = exposure.getInclude();
        includePath.add("*");
        webEndpointProperties.getExposure().setInclude(includePath);
        webEndpointProperties.setBasePath("/");
        ArkletHealthDetailsEndpoint endpoint = new ArkletHealthDetailsEndpoint();
        endpoint.setEndpointService(new ArkletHealthEndpointServiceImpl());
        return endpoint;
    }

    @Bean
    @ConditionalOnMasterBiz
    public ArkletHealthCodeEndpoint arkletHealthCodeEndpoint() {
        ArkletHealthCodeEndpoint endpoint = new ArkletHealthCodeEndpoint();
        endpoint.setEndpointService(new ArkletHealthEndpointServiceImpl());
        return endpoint;
    }

    @Bean
    @ConditionalOnMasterBiz
    @DependsOn("componentRegistry")
    public MasterBizHealthIndicator masterBizHealthIndicator() {
        MasterBizHealthIndicator masterBizHealthIndicator = new MasterBizHealthIndicator();
        masterBizHealthIndicator.setApplicationAvailability(context.getBean(ApplicationAvailability.class));
        ArkletComponentRegistry.getActuatorServiceInstance().registerIndicator(masterBizHealthIndicator);
        return masterBizHealthIndicator;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
