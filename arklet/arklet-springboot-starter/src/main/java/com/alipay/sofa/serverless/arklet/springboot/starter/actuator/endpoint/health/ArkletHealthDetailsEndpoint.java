package com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.health;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.actuator.ActuatorService;
import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.ArkletHealthEndpointService;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.model.EndpointResponse;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.model.EndpointResponseCode;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Map;

/**
 * @author Lunarscave
 */
@Endpoint(id = "arkletHealthDetails")
public class ArkletHealthDetailsEndpoint {

    private static final String HEALTH_ENDPOINT_ERROR_CODE = "endpointError";
    private static final String HEALTH_ERROR_CODE = "error";
    private static final String HEALTH_HEALTHY_CODE = "ACCEPTING_TRAFFIC";

    private ArkletHealthEndpointService endpointService;

    public void setEndpointService(ArkletHealthEndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getHealth() {
        return ArkletHealthDetailsEndpoint.ofResponse(endpointService.getHealth());
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getModuleInfo(@Selector String moduleType) {
        return ArkletHealthDetailsEndpoint.ofResponse(
                endpointService.getModuleInfo(moduleType));
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getModuleInfo(@Selector String moduleType, @Selector String name, @Selector String version) {
        return ArkletHealthDetailsEndpoint.ofResponse(
                endpointService.getModuleInfo(moduleType, name, version));
    }

    private static EndpointResponse<Map<String, Object>> ofResponse(HealthModel healthModel) {
        Map<String, Object> healthData = healthModel.getHealthData();
        EndpointResponse<Map<String, Object>> endpointResponse;
        if (HealthModel.containsError(healthModel, HEALTH_ENDPOINT_ERROR_CODE)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_NOT_FOUND, healthData);
        } else if (HealthModel.containsError(healthModel, HEALTH_ERROR_CODE)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR, healthData);
        } else if (HealthModel.containsUnhealthy(healthModel, HEALTH_HEALTHY_CODE)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.UNHEALTHY, healthData);
        } else {
            endpointResponse = EndpointResponse.ofSuccess(healthData);
        }
        return endpointResponse;
    }
}
