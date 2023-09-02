package com.alipay.sofa.serverless.arklet.core.health.model;




import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class Health {

    private final Map<String, Object> healthData = new HashMap<>();

    public Health() {
    }

    public Health(Map<String, Object> healthData) {
        AssertUtils.assertNotNull(healthData, "health data must not null");
        Health health = Health.createHealth();
        health.setHealthData(healthData);
    }

    public static Health createHealth() {
        return new Health();
    }

    public Map<String, Object> getHealthData() {
        return healthData;
    }

    public void setHealthData(Map<String, ?> healthData) {
        AssertUtils.assertNotNull(healthData, "health data must not null");
        this.healthData.clear();
        this.healthData.putAll(healthData);
    }

    public Health putHealthData(String key, Object value) {
        this.healthData.put(key, value);
        return this;
    }

    public Health putAllHealthData(Map<String, ?> healthData) {
        AssertUtils.assertNotNull(healthData, "health data must not null");
        this.healthData.putAll(healthData);
        return this;
    }

    public Health putAllHealthData(Health healthData) {
        this.healthData.putAll(healthData.getHealthData());
        return this;
    }

    public Health putErrorData(String errorCode, String message) {
        this.healthData.clear();
        this.healthData.put(errorCode, message);
        return this;
    }

    public static boolean containsError(Health health, String errorCode) {
        return health != null && health.healthData.containsKey(errorCode);
    }

    public static boolean containsUnhealthy(Health healthDataModel, String healthyCode) {
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
