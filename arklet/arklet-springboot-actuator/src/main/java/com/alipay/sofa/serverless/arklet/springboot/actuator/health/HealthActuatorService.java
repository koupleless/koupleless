package com.alipay.sofa.serverless.arklet.springboot.actuator.health;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthModel;


/**
 * @author Lunarscave
 */
public interface HealthActuatorService extends ActuatorComponent {

    HealthModel getMasterBizHealth();

    HealthModel getCpuHealth();
}
