package com.alipay.sofa.serverless.arklet.springboot.actuator.health;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.SpringBootUtil;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.CpuIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.JvmIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator.MasterBizIndicator;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import org.springframework.boot.actuate.health.Health;

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
    public Health getMasterBizHealth() {
        updateHealthIndicator();
        return masterBizIndicator.health();
    }

    @Override
    public Health getCpuHealth() {
        updateHealthIndicator();
        return arkletCpuIndicator.health();
    }

    @Override
    public Health getJvmHealth() {
        updateHealthIndicator();
        return jvmIndicator.health();
    }

    private void updateHealthIndicator() {
        this.masterBizIndicator = SpringBootUtil.getBean(MasterBizIndicator.class);
        this.arkletCpuIndicator = SpringBootUtil.getBean(CpuIndicator.class);
        this.jvmIndicator = SpringBootUtil.getBean(JvmIndicator.class);
    }
}
