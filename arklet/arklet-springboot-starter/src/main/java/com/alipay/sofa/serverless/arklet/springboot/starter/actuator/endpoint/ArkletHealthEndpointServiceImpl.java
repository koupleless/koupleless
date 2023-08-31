package com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.actuator.ActuatorService;
import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizModel;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.PluginModel;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;

import javax.inject.Singleton;
import java.util.Set;

public class ArkletHealthEndpointServiceImpl implements ArkletHealthEndpointService {

    private static final String[] MODULE_TYPES = new String[]{"biz", "plugin"};
    private static final String HEALTH_ERROR_CODE = "error";
    private final ActuatorService actuatorService = ArkletComponentRegistry.getActuatorServiceInstance();

    @Override
    public HealthModel getHealth() {
        return HealthModel.createHealthModel()
                .putAllHealthData(actuatorService.getHealth())
                .putAllHealthData(actuatorService.queryMasterBiz())
                .putAllHealthData(actuatorService.queryModuleInfo());
    }

    @Override
    public HealthModel getModuleInfo(String moduleType) {
        HealthModel healthModel = HealthModel.createHealthModel();
        if (MODULE_TYPES[0].equals(moduleType)) {
            healthModel.putAllHealthData(actuatorService.queryModuleInfo(new BizModel()));
        } else if (MODULE_TYPES[1].equals(moduleType)) {
            healthModel.putAllHealthData(actuatorService.queryModuleInfo(new PluginModel()));
        } else {
            healthModel.putErrorData(HEALTH_ERROR_CODE, "module type not in ['biz', 'plugin']");
        }
        return healthModel;
    }

    @Override
    public HealthModel getModuleInfo(String moduleType, String name, String version) {
        HealthModel healthModel = HealthModel.createHealthModel();
        if (MODULE_TYPES[0].equals(moduleType)) {
            BizModel bizModel = new BizModel();
            bizModel.setBizName(name);
            bizModel.setBizVersion(version);
            healthModel.putAllHealthData(actuatorService.queryModuleInfo(bizModel));
        } else if (MODULE_TYPES[1].equals(moduleType)) {
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName(name);
            pluginModel.setPluginVersion(version);
            healthModel.putAllHealthData(actuatorService.queryModuleInfo(pluginModel));
        } else {
            healthModel.putErrorData(HEALTH_ERROR_CODE, "module type not in ['biz', 'plugin']");
        }
        return healthModel;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {

    }
}
