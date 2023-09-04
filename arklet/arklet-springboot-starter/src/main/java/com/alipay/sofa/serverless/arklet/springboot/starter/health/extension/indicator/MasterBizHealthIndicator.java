package com.alipay.sofa.serverless.arklet.springboot.starter.health.extension.indicator;

import com.alipay.sofa.serverless.arklet.core.health.indicator.ArkletBaseIndicator;
import com.alipay.sofa.serverless.arklet.core.health.model.Constants;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;
import org.springframework.boot.availability.ApplicationAvailability;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class MasterBizHealthIndicator extends ArkletBaseIndicator {

    private ApplicationAvailability applicationAvailability;

    private final static String MASTER_BIZ_HEALTH_INDICATOR_ID = Constants.MASTER_BIZ_HEALTH;

    public MasterBizHealthIndicator() {
        super(MASTER_BIZ_HEALTH_INDICATOR_ID);
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        AssertUtils.assertNotNull(applicationAvailability, "applicationAvailability must not null");
        Map<String, Object> masterBizHealthDetails = new HashMap<>(1);
        masterBizHealthDetails.put(MasterBizHealthMetrics.READINESS_STATE.getId(), applicationAvailability.getReadinessState());
        return masterBizHealthDetails;
    }

    public void setApplicationAvailability(ApplicationAvailability applicationAvailability) {
        this.applicationAvailability = applicationAvailability;
    }

    enum MasterBizHealthMetrics {

        READINESS_STATE("readinessState");

        private final String id;

        MasterBizHealthMetrics(String desc) {
            this.id = desc;
        }

        public String getId(){
            return id;
        };
    }
}
