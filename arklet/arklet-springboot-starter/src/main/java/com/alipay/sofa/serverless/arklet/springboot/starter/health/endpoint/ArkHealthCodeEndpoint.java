package com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.health.HealthService;
import com.alipay.sofa.serverless.arklet.core.health.model.Constants;
import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint.model.EndpointResponseCode;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;


/**
 * @author Lunarscave
 */
@Endpoint(id = "arkHealthCode")
public class ArkHealthCodeEndpoint {

    private final HealthService healthService = ArkletComponentRegistry.getHealthServiceInstance();

    @ReadOperation
    public int healthCode(){
        return ArkHealthCodeEndpoint.ofCode(Health.createHealth()
                .putAllHealthData(healthService.getHealth())
                .putAllHealthData(healthService.queryModuleInfo()));
    }

    @ReadOperation
    public int getModuleInfoHealthCode(@Selector String moduleType) {
        return ArkHealthCodeEndpoint.ofCode(healthService.queryModuleInfo(moduleType, null, null));
    }

    @ReadOperation
    public int getModuleInfoHealthCode(@Selector String moduleType, @Selector String name, @Selector String version) {
        return ArkHealthCodeEndpoint.ofCode(healthService.queryModuleInfo(moduleType, name, version));
    }

    public static int ofCode(Health health) {
        int endpointCode;
        if (Health.containsError(health, Constants.HEALTH_ENDPOINT_ERROR)) {
            endpointCode = EndpointResponseCode.ENDPOINT_NOT_FOUND.getCode();
        } else if (Health.containsError(health, Constants.HEALTH_ERROR)) {
            endpointCode = EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR.getCode();
        } else if (Health.containsUnhealthy(health, Constants.READINESS_HEALTHY)) {
            endpointCode = EndpointResponseCode.UNHEALTHY.getCode();
        } else {
            endpointCode = EndpointResponseCode.HEALTHY.getCode();
        }
        return endpointCode;
    }
}
