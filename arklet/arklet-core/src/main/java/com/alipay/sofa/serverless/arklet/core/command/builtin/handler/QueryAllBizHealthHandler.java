package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDataModel;

/**
 * @author Lunarscave
 */
public class QueryAllBizHealthHandler extends AbstractCommandHandler<InputMeta, HealthDataModel> {
    @Override
    public void validate(InputMeta inputMeta) throws CommandValidationException {
    }

    @Override
    public Output<HealthDataModel> handle(InputMeta inputMeta) {
        return Output.ofSuccess(getOperationService().queryAllBizHealth());
    }

    @Override
    public Command command() {
        return BuiltinCommand.QUERY_ALL_BIZ_HEALTH;
    }
}
