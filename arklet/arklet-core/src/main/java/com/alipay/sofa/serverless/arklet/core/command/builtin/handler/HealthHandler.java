package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.serverless.arklet.core.health.model.Constants;
import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Lunarscave
 */
public class HealthHandler extends AbstractCommandHandler<HealthHandler.Input, Health> {
    @Override
    public void validate(Input input) throws CommandValidationException {
        if (input != null) {
            String type = input.getType();
            if (!StringUtils.isEmpty(input.getType())) {
                isTrue(Constants.typeOfQuery(type), "type: %s can not be found", type);
            }
        }
    }

    @Override
    public Output<Health> handle(Input input) {
        Health health = Health.createHealth();
        input = input == null ? new Input() : input;
        String type = input.getType();

        // set query strategy
        if (StringUtils.isEmpty(type)) {
            health.putAllHealthData(getHealthService().queryMasterBiz());
        }
        if (StringUtils.isEmpty(type) || Constants.SYSTEM.equals(type)) {
            health.putAllHealthData(getHealthService().getHealth(input.getMetrics()));
        }
        if (StringUtils.isEmpty(type) || Constants.typeOfInfo(type)) {
            String name = input.getModuleName();
            String version = input.getModuleVersion();
            health.putAllHealthData(getHealthService().queryModuleInfo(type, name, version));
        }

        if (Health.containsError(health, Constants.HEALTH_ERROR)) {
            return Output.ofFailed(health.getHealthData().get(Constants.HEALTH_ERROR).toString());
        } else {
            return Output.ofSuccess(health);
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
        private String moduleName;
        private String moduleVersion;
        private String[] metrics;
    }
}
