package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.health.handler.CpuHandler;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

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
    protected HealthDetailsModel getHealthInfo() {
        HealthDetailsModel cpuHealthModel = new HealthDetailsModel(getIndicatorId());

        cpuHandler.collectTicks();
        cpuHealthModel.putHealthData("count", cpuHandler.getCpuCount());
        cpuHealthModel.putHealthData("type", cpuHandler.getCpuType());
        cpuHealthModel.putHealthData("total used (%)", cpuHandler.getTotalUsed());
        cpuHealthModel.putHealthData("user used (%)", cpuHandler.getUserUsed());
        cpuHealthModel.putHealthData("system used (%)", cpuHandler.getSystemUsed());
        cpuHealthModel.putHealthData("free (%)", cpuHandler.getFree());
        return cpuHealthModel;
    }

    @Override
    public Health health() {
        try {
            HealthDetailsModel cpuHealthModel = getHealthInfo();
            return Health.up().withDetail(getIndicatorId(), cpuHealthModel).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
