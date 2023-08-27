package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthResponseModel;

import java.util.List;

/**
 * @author Lunarscave
 */
public class HealthHandler extends AbstractCommandHandler<InputMeta, HealthResponseModel> {
    @Override
    public void validate(InputMeta inputMeta) throws CommandValidationException {
    }

    @Override
    public Output<HealthResponseModel> handle(InputMeta inputMeta) {
        return Output.ofSuccess(getOperationService().health());
    }

    @Override
    public Command command() {
        return BuiltinCommand.HEALTH;
    }
}
