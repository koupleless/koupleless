package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * @author Lunarscave
 */
public abstract class ArkletIndicator implements HealthIndicator {

    private final String indicatorId;

    protected abstract HealthDetailsModel getHealthInfo();

    protected String getIndicatorId() {
        return indicatorId;
    }

    protected  ArkletIndicator(String indicatorId) {
        this.indicatorId = indicatorId;
    }
}
