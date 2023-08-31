package com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.health;

import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.ArkletHealthEndpointService;
import com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.model.EndpointResponseCode;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;


/**
 * @author Lunarscave
 */
@Endpoint(id = "arkletHealthCode")
public class ArkletHealthCodeEndpoint {

    private static final String HEALTH_ENDPOINT_ERROR_CODE = "endpointError";
    private static final String HEALTH_ERROR_CODE = "error";
    private static final String HEALTH_HEALTHY_CODE = "ACCEPTING_TRAFFIC";

    private ArkletHealthEndpointService endpointService;

    public void setEndpointService(ArkletHealthEndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @ReadOperation
    public int healthCode(){
        return ArkletHealthCodeEndpoint.ofCode(endpointService.getHealth());
    }

    @ReadOperation
    public int getModuleInfoHealthCode(@Selector String moduleType) {
        return ArkletHealthCodeEndpoint.ofCode(endpointService.getModuleInfo(moduleType));
    }

    @ReadOperation
    public int getModuleInfoHealthCode(@Selector String moduleType, @Selector String name, @Selector String version) {
        return ArkletHealthCodeEndpoint.ofCode(endpointService.getModuleInfo(moduleType, name, version));
    }

    public static int ofCode(HealthModel healthModel) {
        int endpointCode;
        if (HealthModel.containsError(healthModel, HEALTH_ENDPOINT_ERROR_CODE)) {
            endpointCode = EndpointResponseCode.ENDPOINT_NOT_FOUND.getCode();
        } else if (HealthModel.containsError(healthModel, HEALTH_ERROR_CODE)) {
            endpointCode = EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR.getCode();
        } else if (HealthModel.containsUnhealthy(healthModel, HEALTH_HEALTHY_CODE)) {
            endpointCode = EndpointResponseCode.UNHEALTHY.getCode();
        } else {
            endpointCode = EndpointResponseCode.HEALTHY.getCode();
        }
        return endpointCode;
    }
}
