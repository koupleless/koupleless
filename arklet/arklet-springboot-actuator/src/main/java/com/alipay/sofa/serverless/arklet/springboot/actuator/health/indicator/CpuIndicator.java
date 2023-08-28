package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.health.handler.CpuHandler;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
@Component
public class CpuIndicator extends ArkletIndicator {

    private final CpuHandler cpuHandler;

    protected CpuIndicator() {
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

    @Override
    public Health health() {
        try {
            Map<String, Object> cpuHealthDetails = getHealthDetails();
            return Health.up().withDetails(cpuHealthDetails).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
