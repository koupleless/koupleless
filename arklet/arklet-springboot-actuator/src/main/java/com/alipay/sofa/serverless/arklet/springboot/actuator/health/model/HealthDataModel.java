package com.alipay.sofa.serverless.arklet.springboot.actuator.health.model;


import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.AssertUtil;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class HealthDataModel {

    private final Map<String, Object> healthData;

    public HealthDataModel() {
        this.healthData = new HashMap<>();
    }

    public static HealthDataModel createHealthDataModel(Map<String, Object> healthData) {
        AssertUtil.assertNotNull(healthData, "health data must not null");
        HealthDataModel healthDataModel = new HealthDataModel();
        healthDataModel.setHealthData(healthData);
        return healthDataModel;
    }

    public static HealthDataModel createHealthDataModel() {
        return new HealthDataModel();
    }

    public Map<String, Object> getHealthData() {
        return healthData;
    }

    public void setHealthData(Map<String, ?> healthData) {
        AssertUtil.assertNotNull(healthData, "health data must not null");
        this.healthData.clear();
        this.healthData.putAll(healthData);
    }

    public HealthDataModel putHealthData(String key, Object value) {
        this.healthData.put(key, value);
        return this;
    }

    public HealthDataModel putAllHealthData(Map<String, ?> healthData) {
        AssertUtil.assertNotNull(healthData, "health data must not null");
        this.healthData.putAll(healthData);
        return this;
    }

    public HealthDataModel putAllHealthData(HealthDataModel healthData) {
        this.healthData.putAll(healthData.getHealthData());
        return this;
    }

    public HealthDataModel putHealthData(String healthId, Health health) {
        AssertUtil.assertNotNull(health.getStatus(), "health status must not null");
        AssertUtil.assertNotNull(health.getDetails(), "health details must not null");
        Map<String, Object> healthMap = new HashMap<>(health.getDetails());

        Map<String, Object> healthStatus = new HashMap<>(2);
        healthStatus.put("code", health.getStatus().getCode());
        healthStatus.put("description", health.getStatus().getDescription());
        healthMap.put("health", healthStatus);

        this.healthData.put(healthId, healthMap);
        return this;
    }
}
