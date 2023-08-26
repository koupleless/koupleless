package com.alipay.sofa.serverless.arklet.springboot.actuator.health.model;

import org.springframework.boot.actuate.health.Health;

/**
 * @author Lunarscave
 */
public class HealthModel {

    private String healthId;

    private Object healthData;

    public HealthModel() {

    }

    public HealthModel(String healthId, Object healthData) {
        this.healthId= healthId;
        this.healthData = healthData;
    }

    public String getHealthId() {
        return healthId;
    }

    public Object getHealthData() {
        return healthData;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public void setHealthData(Object healthData) {
        this.healthData = healthData;
    }
}
