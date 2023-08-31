package com.alipay.sofa.serverless.arklet.core.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.core.actuator.health.handler.CpuHandler;
import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class CpuIndicator extends ArkletBaseIndicator {

    private final CpuHandler cpuHandler;

    public CpuIndicator() {
        super("cpu");
        cpuHandler = new CpuHandler();
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        Map<String, Object> cpuHealthDetails = new HashMap<>(6);

        cpuHandler.collectTicks();
        cpuHealthDetails.put("count", cpuHandler.getCpuCount());
        cpuHealthDetails.put("type", cpuHandler.getCpuType());
        cpuHealthDetails.put("total used (%)", cpuHandler.getTotalUsed());
        cpuHealthDetails.put("user used (%)", cpuHandler.getUserUsed());
        cpuHealthDetails.put("system used (%)", cpuHandler.getSystemUsed());
        cpuHealthDetails.put("free (%)", cpuHandler.getFree());
        return cpuHealthDetails;
    }
}
