package com.alipay.sofa.serverless.arklet.core.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;

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

    public HealthModel getHealthModel() {
        return HealthModel.createHealthModel().putHealthData(getIndicatorId(), getHealthDetails());
    }
}
