package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndicatorAutoConfiguration {
    @Bean
    public ArkletCpuIndicator arkletCpuIndicator() {
        return new ArkletCpuIndicator();
    }

    @Bean
    public MasterBizIndicator masterBizIndicator() {
        return new MasterBizIndicator();
    }
}
