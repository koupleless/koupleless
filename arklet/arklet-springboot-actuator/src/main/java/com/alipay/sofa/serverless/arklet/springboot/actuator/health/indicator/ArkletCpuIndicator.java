package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

/**
 * @author Lunarscave
 */
@Component
public class ArkletCpuIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            SystemInfo si = new SystemInfo();
            CentralProcessor cpu = si.getHardware().getProcessor();
            return Health.up().
                    withDetail("cpuid", cpu.getProcessorIdentifier().getName()).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
