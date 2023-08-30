package com.alipay.sofa.serverless.arklet.springboot.actuator.endpoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lunarscave
 */
@Configuration
public class ArkletHealthEndpointAutoConfiguration {
    @Bean
    public ArkletHealthEndpoint arkletHealthEndpoint() {
        return new ArkletHealthEndpoint();
    }
}
