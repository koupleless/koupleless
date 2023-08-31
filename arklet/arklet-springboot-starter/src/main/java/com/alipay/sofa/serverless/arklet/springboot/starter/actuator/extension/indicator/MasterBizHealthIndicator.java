package com.alipay.sofa.serverless.arklet.springboot.starter.actuator.extension.indicator;

import com.alipay.sofa.serverless.arklet.core.actuator.health.indicator.ArkletBaseIndicator;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;
import org.springframework.boot.availability.ApplicationAvailability;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class MasterBizHealthIndicator extends ArkletBaseIndicator {

    private ApplicationAvailability applicationAvailability;


    public MasterBizHealthIndicator() {
        super("masterBizHealth");
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        AssertUtils.assertNotNull(applicationAvailability, "applicationAvailability must not null");
        Map<String, Object> masterBizHealthDetails = new HashMap<>(1);
        masterBizHealthDetails.put("readinessState", applicationAvailability.getReadinessState());
        return masterBizHealthDetails;
    }

    public void setApplicationAvailability(ApplicationAvailability applicationAvailability) {
        this.applicationAvailability = applicationAvailability;
    }

    //    @Override
//    public Health health() {
//        try {
//            Map<String, Object> masterBizHealthDetails = getHealthDetails();
//            return Health.up().withDetails(masterBizHealthDetails).build();
//        } catch (Exception e) {
//            return Health.down(e).build();
//        }
//    }
}
