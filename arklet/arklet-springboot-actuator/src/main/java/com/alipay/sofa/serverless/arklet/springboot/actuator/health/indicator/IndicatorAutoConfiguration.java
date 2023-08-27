package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lunarscave
 */
@Configuration
public class IndicatorAutoConfiguration {
    @Bean
    public CpuIndicator cpuIndicator() {
        return new CpuIndicator();
    }

    @Bean
    public MasterBizIndicator masterBizIndicator() {
        return new MasterBizIndicator();
    }

    @Bean
    public JvmIndicator jvmIndicator() {return new JvmIndicator();}
}
