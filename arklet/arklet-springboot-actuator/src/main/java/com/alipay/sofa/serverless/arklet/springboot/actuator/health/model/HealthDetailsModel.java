package com.alipay.sofa.serverless.arklet.springboot.actuator.health.model;


import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.AssertUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class HealthDetailsModel {

    private String healthId;

    private final Map<String, Object> healthData;

    public HealthDetailsModel() {
        healthData = new HashMap<>();
    }

    public HealthDetailsModel(String healthId) {
        healthData = new HashMap<>();
        this.healthId = healthId;
    }

    public HealthDetailsModel(String healthId, Map<? extends String, Object> healthDataMap) {
        AssertUtil.assertNotNull(healthDataMap, "health data must not null");
        this.healthData = new HashMap<>();
        this.healthId = healthId;
        this.healthData.putAll(healthDataMap);
    }

    public String getHealthId() {
        return healthId;
    }

    public Map<String, Object> getHealthData() {
        return healthData;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public void setHealthData(Map<? extends String, ?> healthData) {
        AssertUtil.assertNotNull(healthData, "health data must not null");
        this.healthData.clear();
        this.healthData.putAll(healthData);
    }

    public void putHealthData(String key, Object value) {
        this.healthData.put(key, value);
    }


}
