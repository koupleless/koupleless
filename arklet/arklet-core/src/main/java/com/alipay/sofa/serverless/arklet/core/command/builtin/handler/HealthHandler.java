package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.serverless.arklet.core.actuator.ActuatorService;
import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;
import com.alipay.sofa.serverless.arklet.core.actuator.model.QueryType;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizModel;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.PluginModel;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;

/**
 * @author Lunarscave
 */
public class HealthHandler extends AbstractCommandHandler<HealthHandler.Input, HealthModel> {
    @Override
    public void validate(Input input) throws CommandValidationException {
        if (input != null) {
            String type = input.getType();
            if (type != null) {
                isTrue(QueryType.containsId(type), "type: %s can not be found", type);
            }
        }
    }

    @Override
    public Output<HealthModel> handle(Input input) {
        HealthModel healthModel = HealthModel.createHealthModel();
        input = input == null ? new Input() : input;
        String type = input.getType();

        // set query strategy
        if (type == null) {
            healthModel.putAllHealthData(getActuatorService().queryMasterBiz());
        }
        if (type == null || QueryType.SYSTEM.getId().equals(type)) {
            String[] metrics = input.getMetrics();
            if (metrics == null) {
                healthModel.putAllHealthData(getActuatorService().getHealth());
            } else {
                for (String metric : metrics) {
                    healthModel.putAllHealthData(getActuatorService().getHealth(metric));
                }
            }
        }
        if (type == null || QueryType.BIZ.getId().equals(type)) {
            BizModel bizModel = new BizModel();
            bizModel.setBizName(input.getBizName());
            bizModel.setBizVersion(input.getBizVersion());
            healthModel.putAllHealthData(getActuatorService().queryModuleInfo(bizModel));
        }
        if (type == null || QueryType.PLUGIN.getId().equals(type)) {
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName(input.getPluginName());
            healthModel.putAllHealthData(getActuatorService().queryModuleInfo(pluginModel));
        }

        if (HealthModel.containsError(healthModel, "error")) {
            return Output.ofFailed(healthModel.getHealthData().get("error").toString());
        } else {
            return Output.ofSuccess(healthModel);
        }
    }

    @Override
    public Command command() {
        return BuiltinCommand.HEALTH;
    }

    @Getter
    @Setter
    public static class Input extends ArkBizMeta {
        private String type;
        private String bizName;
        private String bizVersion;
        private String pluginName;
        private String[] metrics;
    }
}
