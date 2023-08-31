package com.alipay.sofa.serverless.arklet.core.actuator.model;




import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class HealthModel {

    private final Map<String, Object> healthData;

    public HealthModel() {
        this.healthData = new HashMap<>();
    }

    public static HealthModel createHealthModel(Map<String, Object> healthData) {
        AssertUtils.assertNotNull(healthData, "health data must not null");
        HealthModel healthModel = HealthModel.createHealthModel();
        healthModel.setHealthData(healthData);
        return healthModel;
    }

    public static HealthModel createHealthModel() {
        HealthModel healthModel = new HealthModel();
        return healthModel;
    }

    public Map<String, Object> getHealthData() {
        return healthData;
    }

    public void setHealthData(Map<String, ?> healthData) {
        AssertUtils.assertNotNull(healthData, "health data must not null");
        this.healthData.clear();
        this.healthData.putAll(healthData);
    }

    public HealthModel putHealthData(String key, Object value) {
        this.healthData.put(key, value);
        return this;
    }

    public HealthModel putAllHealthData(Map<String, ?> healthData) {
        AssertUtils.assertNotNull(healthData, "health data must not null");
        this.healthData.putAll(healthData);
        return this;
    }

    public HealthModel putAllHealthData(HealthModel healthData) {
        this.healthData.putAll(healthData.getHealthData());
        return this;
    }

    public HealthModel putErrorData(String errorCode, String message) {
        this.healthData.clear();
        this.healthData.put(errorCode, message);
        return this;
    }

    public static boolean containsError(HealthModel healthModel, String errorCode) {
        return healthModel != null && healthModel.healthData.containsKey(errorCode);
    }

    public static boolean containsUnhealthy(HealthModel healthDataModel, String healthyCode) {
        Map<String, Object> healthData = healthDataModel.getHealthData();
        boolean isUnhealthy = false;
        for (String key: healthData.keySet()) {
            Object value = healthData.get(key);
            if (value instanceof Map) {
                if (((Map<?, ?>) value).containsKey("masterBizHealth")){
                    Object health =  ((Map<?, ?>) value).get("masterBizHealth");
                    if (health instanceof Map) {
                        String code = (String) ((Map<?, ?>) health).get("readinessState");
                        if (!code.equals(healthyCode)) {
                            isUnhealthy = true;
                            break;
                        }
                    }
                }
            }
        }
        return isUnhealthy;
    }

}
