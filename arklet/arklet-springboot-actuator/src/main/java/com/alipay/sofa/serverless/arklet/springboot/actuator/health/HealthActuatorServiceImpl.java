package com.alipay.sofa.serverless.arklet.springboot.actuator.health;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.SpringBootUtil;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.ArkletCpuIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.MasterBizIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthModel;

/**
 * @author Lunarscave
 */

public class HealthActuatorServiceImpl implements HealthActuatorService {

    private MasterBizIndicator masterBizIndicator;
    private ArkletCpuIndicator arkletCpuIndicator;

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public HealthModel getMasterBizHealth() {
        updateHealthIndicator();
        return new HealthModel("master-biz-health", masterBizIndicator.health());
    }

    @Override
    public HealthModel getCpuHealth() {
        updateHealthIndicator();
        return new HealthModel("cpu-health", arkletCpuIndicator.health());
    }

    private void updateHealthIndicator() {
        this.masterBizIndicator = SpringBootUtil.getBean(MasterBizIndicator.class);
        this.arkletCpuIndicator = SpringBootUtil.getBean(ArkletCpuIndicator.class);
    }
}
