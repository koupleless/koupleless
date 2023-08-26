package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Lunarscave
 */
@Component
public class MasterBizIndicator implements HealthIndicator {

    @Autowired
    private ApplicationAvailability applicationAvailability;

    @Override
    public Health health() {
        try {
            Assert.notNull(applicationAvailability, "applicationAvailability must not null");
            return Health.up().
                    withDetail("readiness-state", applicationAvailability.getReadinessState()).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
