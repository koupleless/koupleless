package com.alipay.sofa.serverless.arklet.springboot.actuator.health;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDataModel;
import org.springframework.boot.actuate.health.Health;


/**
 * @author Lunarscave
 */
public interface HealthActuatorService extends ActuatorComponent {

    HealthDataModel getMasterBizHealth();

    HealthDataModel getCpuHealth();

    HealthDataModel getJvmHealth();
}
