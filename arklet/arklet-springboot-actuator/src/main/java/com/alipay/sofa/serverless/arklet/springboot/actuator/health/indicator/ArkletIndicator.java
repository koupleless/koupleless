package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Map;

/**
 * @author Lunarscave
 */
public abstract class ArkletIndicator implements HealthIndicator {

    private final String indicatorId;

    protected abstract Map<String, Object> getHealthDetails();

    public String getIndicatorId() {
        return indicatorId;
    }

    protected  ArkletIndicator(String indicatorId) {
        this.indicatorId = indicatorId;
    }
}
