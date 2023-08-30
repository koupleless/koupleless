package com.alipay.sofa.serverless.arklet.springboot.actuator.endpoint;

import com.alipay.sofa.serverless.arklet.springboot.actuator.api.ActuatorClient;
import com.alipay.sofa.serverless.arklet.springboot.actuator.api.HealthQueryType;
import com.alipay.sofa.serverless.arklet.springboot.actuator.endpoint.model.EndpointResponse;
import com.alipay.sofa.serverless.arklet.springboot.actuator.endpoint.model.EndpointResponseCode;
import com.alipay.sofa.serverless.arklet.springboot.actuator.model.HealthDataModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Map;

/**
 * @author Lunarscave
 */
@Endpoint(id = "arkletHealth")
public class ArkletHealthEndpoint {

    private static final String[] MODULE_TYPES = new String[]{"biz", "plugin"};
    private static final String HEALTH_ERROR_CODE = "endpointError";

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getAllHealth(){
        HealthDataModel healthData = ActuatorClient.getHealth(HealthQueryType.ALL);
        return ArkletHealthEndpoint.ofResponse(healthData);
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getListHealth(@Selector String moduleType ) {
        HealthDataModel healthData = HealthDataModel.createHealthDataModel();
        if (MODULE_TYPES[0].equals(moduleType)) {
            healthData.putAllHealthData(ActuatorClient.getHealth(HealthQueryType.BIZ_LIST));
        } else if (MODULE_TYPES[1].equals(moduleType)) {
            healthData.putAllHealthData(ActuatorClient.getHealth(HealthQueryType.PLUGIN_LIST));
        } else {
            healthData.putErrorData(HEALTH_ERROR_CODE, "module type not in ['biz', 'plugin']");
        }
        return ArkletHealthEndpoint.ofResponse(healthData);
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getTypeAndName(@Selector String moduleType, @Selector String name, @Selector String version) {
        HealthDataModel healthData = HealthDataModel.createHealthDataModel();
        if (MODULE_TYPES[0].equals(moduleType)) {
            healthData.putAllHealthData(ActuatorClient.getHealth(BizModel.createBizModel(name, version)));
        } else if (MODULE_TYPES[1].equals(moduleType)) {
            healthData.putAllHealthData(ActuatorClient.getHealth(PluginModel.createPluginModel(name, version)));
        } else {
            healthData.putErrorData(HEALTH_ERROR_CODE, "module type not in ['biz', 'plugin']");
        }
        return ArkletHealthEndpoint.ofResponse(healthData);
    }

    private static EndpointResponse<Map<String, Object>> ofResponse(HealthDataModel healthDataModel) {
        Map<String, Object> healthData = healthDataModel.getHealthData();
        EndpointResponse<Map<String, Object>> endpointResponse;
        if (HealthDataModel.containsError(healthDataModel, HEALTH_ERROR_CODE)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_NOT_FOUND, healthData);
        } else if (HealthDataModel.containsError(healthDataModel, "error")) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR, healthData);
        } else if (HealthDataModel.containsUnhealthy(healthDataModel)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_UNHEALTHY, healthData);
        } else {
            endpointResponse = EndpointResponse.ofSuccess(healthData);
        }
        return endpointResponse;
    }
}
