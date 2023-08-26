package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizModel;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.springboot.actuator.api.ActuatorClient;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class HealthHandler extends AbstractCommandHandler<InputMeta, List<HealthModel>> {
    @Override
    public void validate(InputMeta inputMeta) throws CommandValidationException {
    }

    @Override
    public Output<List<HealthModel>> handle(InputMeta inputMeta) {
        return Output.ofSuccess(getOperationService().health());
    }

    @Override
    public Command command() {
        return BuiltinCommand.HEALTH;
    }
}
