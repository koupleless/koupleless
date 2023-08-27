package com.alipay.sofa.serverless.arklet.springboot.actuator.health;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import org.springframework.boot.actuate.health.Health;


/**
 * @author Lunarscave
 */
public interface HealthActuatorService extends ActuatorComponent {

    Health getMasterBizHealth();

    Health getCpuHealth();

    Health getJvmHealth();
}
