package com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint;

import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

public interface ArkletHealthEndpointService extends ArkletComponent {
    HealthModel getHealth();

    HealthModel getModuleInfo(String moduleType);

    HealthModel getModuleInfo(String moduleType, String name, String version);
}
