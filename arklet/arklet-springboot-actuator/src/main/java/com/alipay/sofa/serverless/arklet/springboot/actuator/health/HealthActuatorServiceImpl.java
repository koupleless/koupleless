package com.alipay.sofa.serverless.arklet.springboot.actuator.health;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.SpringBootUtil;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.CpuIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.JvmIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.MasterBizIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.model.HealthDataModel;

/**
 * @author Lunarscave
 */

public class HealthActuatorServiceImpl implements HealthActuatorService {

    private MasterBizIndicator masterBizIndicator;
    private CpuIndicator arkletCpuIndicator;
    private JvmIndicator jvmIndicator;

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public HealthDataModel getMasterBizHealth() {
        updateHealthIndicator();
        return HealthDataModel.createHealthDataModel()
                .putHealthData(masterBizIndicator.getIndicatorId(), masterBizIndicator.health());
    }

    @Override
    public HealthDataModel getCpuHealth() {
        updateHealthIndicator();
        return HealthDataModel.createHealthDataModel()
                .putHealthData(arkletCpuIndicator.getIndicatorId(), arkletCpuIndicator.health());
    }

    @Override
    public HealthDataModel getJvmHealth() {
        updateHealthIndicator();
        return HealthDataModel.createHealthDataModel()
                .putHealthData(jvmIndicator.getIndicatorId(), jvmIndicator.health());
    }

    private void updateHealthIndicator() {
        this.masterBizIndicator = SpringBootUtil.getBean(MasterBizIndicator.class);
        this.arkletCpuIndicator = SpringBootUtil.getBean(CpuIndicator.class);
        this.jvmIndicator = SpringBootUtil.getBean(JvmIndicator.class);
    }
}
