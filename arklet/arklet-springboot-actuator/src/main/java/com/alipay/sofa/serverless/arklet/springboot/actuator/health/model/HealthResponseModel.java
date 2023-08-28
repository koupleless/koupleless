package com.alipay.sofa.serverless.arklet.springboot.actuator.health.model;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.AssertUtil;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class HealthResponseModel {

    private final Map<String, Object> healthInfo;

    public HealthResponseModel() {
        healthInfo = new HashMap<>();
    }

    public Map<String, Object> getHealthInfo() {
        return healthInfo;
    }

    public void setHealthInfo(Map<? extends String, ?> healthInfo) {
        AssertUtil.assertNotNull(healthInfo, "health data must not null");
        this.healthInfo.clear();
        this.healthInfo.putAll(healthInfo);
    }

    public void putHealthInfo(HealthDetailsModel detailsModel) {
        AssertUtil.assertNotNull(detailsModel.getHealthId(), "health details id must not null");
        AssertUtil.assertNotNull(detailsModel.getHealthData(), "health details data must not null");
        this.healthInfo.put(detailsModel.getHealthId(), detailsModel.getHealthData());
    }

    public void putHealthInfo(Health health) {
        AssertUtil.assertNotNull(health.getStatus(), "health status must not null");
        AssertUtil.assertNotNull(health.getDetails(), "health details must not null");
        final String healthKey = "health";
        if (this.healthInfo.containsKey(healthKey) && this.healthInfo.get(healthKey).equals(Status.UP)) {
            this.healthInfo.put(healthKey, health.getStatus());
        }
            Map<String, Object> healthMap = health.getDetails();
            for (String key : healthMap.keySet()) {
                HealthDetailsModel detailsModel = (HealthDetailsModel) healthMap.get(key);
                this.healthInfo.put(key, detailsModel.getHealthData());
            }
    }

}
