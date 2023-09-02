package com.alipay.sofa.serverless.arklet.core.health.indicator;

import com.alipay.sofa.serverless.arklet.core.health.model.Health;

import java.util.Map;

/**
 * @author Lunarscave
 */
public abstract class ArkletBaseIndicator {

    private final String indicatorId;

    public ArkletBaseIndicator(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    protected abstract Map<String, Object> getHealthDetails();

    public String getIndicatorId() {
        return indicatorId;
    }

    public Health getHealthModel() {
        return Health.createHealth().putHealthData(getIndicatorId(), getHealthDetails());
    }
}
