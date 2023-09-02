package com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.health.HealthService;
import com.alipay.sofa.serverless.arklet.core.health.model.Constants;
import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint.model.EndpointResponse;
import com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint.model.EndpointResponseCode;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Map;

/**
 * @author Lunarscave
 */
@Endpoint(id = "arkHeathz")
public class ArkHealthzEndpoint {

    private final HealthService healthService = ArkletComponentRegistry.getHealthServiceInstance();

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getHealth() {
        return ArkHealthzEndpoint.ofResponse(Health.createHealth()
                .putAllHealthData(healthService.getHealth())
                .putAllHealthData(healthService.queryModuleInfo()));
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getModuleInfo(@Selector String moduleType) {
        return ArkHealthzEndpoint.ofResponse(healthService.queryModuleInfo(moduleType, null, null));
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getModuleInfo(@Selector String moduleType, @Selector String name, @Selector String version) {
        return ArkHealthzEndpoint.ofResponse(healthService.queryModuleInfo(moduleType, name, version));
    }

    private static EndpointResponse<Map<String, Object>> ofResponse(Health health) {
        Map<String, Object> healthData = health.getHealthData();
        EndpointResponse<Map<String, Object>> endpointResponse;
        if (Health.containsError(health, Constants.HEALTH_ENDPOINT_ERROR)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_NOT_FOUND, healthData);
        } else if (Health.containsError(health, Constants.HEALTH_ERROR)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR, healthData);
        } else if (Health.containsUnhealthy(health, Constants.READINESS_HEALTHY)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.UNHEALTHY, healthData);
        } else {
            endpointResponse = EndpointResponse.ofSuccess(healthData);
        }
        return endpointResponse;
    }
}
