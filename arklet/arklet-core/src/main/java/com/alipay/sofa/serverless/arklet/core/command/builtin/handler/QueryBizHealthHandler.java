package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDataModel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Lunarscave
 */
public class QueryBizHealthHandler extends AbstractCommandHandler<QueryBizHealthHandler.Input, HealthDataModel> {
    @Override
    public void validate(Input input) throws CommandValidationException {
        notBlank(input.getBizName(), "bizName should not be blank");
        notBlank(input.getBizVersion(), "bizVersion should not be blank");
    }

    @Override
    public Output<HealthDataModel> handle(Input input) {
        return Output.ofSuccess(getOperationService().
                queryBizHealth(input.getBizName(), input.getBizVersion()));
    }

    @Override
    public Command command() {
        return BuiltinCommand.QUERY_BIZ_HEALTH;
    }

    @Getter
    @Setter
    public static class Input extends ArkBizMeta {
        private String bizName;
        private String bizVersion;
    }
}