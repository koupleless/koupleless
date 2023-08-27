package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.AssertUtil;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Component;

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
    protected HealthDetailsModel getHealthInfo() {
        AssertUtil.assertNotNull(applicationAvailability, "applicationAvailability must not null");
        HealthDetailsModel masterBizHealthModel = new HealthDetailsModel(getIndicatorId());
        masterBizHealthModel.putHealthData("readinessState", applicationAvailability.getReadinessState());
        return masterBizHealthModel;
    }

    @Override
    public Health health() {
        try {
            HealthDetailsModel masterBizHealthModel = getHealthInfo();
            return Health.up().withDetail(getIndicatorId(), masterBizHealthModel).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
