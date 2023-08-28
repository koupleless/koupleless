package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
@Component
public class MasterBizIndicator extends ArkletIndicator {

    @Autowired
    private ApplicationAvailability applicationAvailability;

    protected MasterBizIndicator() {
        super("masterBizHealth");
    }


    @Override
    protected Map<String, Object> getHealthDetails() {
        AssertUtil.assertNotNull(applicationAvailability, "applicationAvailability must not null");
        Map<String, Object> masterBizHealthDetails = new HashMap<>(1);
        masterBizHealthDetails.put("readinessState", applicationAvailability.getReadinessState());
        return masterBizHealthDetails;
    }

    @Override
    public Health health() {
        try {
            Map<String, Object> masterBizHealthDetails = getHealthDetails();
            return Health.up().withDetails(masterBizHealthDetails).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
