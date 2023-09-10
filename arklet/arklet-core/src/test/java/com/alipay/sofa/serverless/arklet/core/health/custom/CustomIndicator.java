package com.alipay.sofa.serverless.arklet.core.health.custom;

import com.alipay.sofa.serverless.arklet.core.health.indicator.ArkletBaseIndicator;

import java.util.HashMap;
import java.util.Map;

public class CustomIndicator extends ArkletBaseIndicator {
    public CustomIndicator() {
        super("custom");
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        Map<String, Object> cpuHealthDetails = new HashMap<>();
        cpuHealthDetails.put("key", "value");
        return cpuHealthDetails;
    }
}
