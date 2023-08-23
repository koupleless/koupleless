package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.util.BizIdentityUtils;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.UninstallBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.coordinate.BizCommandCoordinator;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.common.exception.ArkletRuntimeException;
import lombok.Getter;
import lombok.Setter;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class UninstallBizHandler extends AbstractCommandHandler<Input, Void> {

    @Override
    public Output<Void> handle(Input input) {
        try {
            boolean conflict = BizCommandCoordinator.existBizProcessing(input.getBizName(), input.getBizVersion());
            if (conflict) {
                return Output.ofFailed(ResponseCode.FAILED.name() + ":" + String.format("%s uninstall conflict, exist unfinished command for this biz",
                    BizIdentityUtils.generateBizIdentity(input.getBizName(), input.getBizVersion())));
            }
            BizCommandCoordinator.putBizExecution(input.getBizName(), input.getBizVersion(), command());
            ClientResponse res = getOperationService().uninstall(input.getBizName(), input.getBizVersion());
            if (ResponseCode.SUCCESS.equals(res.getCode())) {
                return Output.ofSuccess(null);
            } else {
                return Output.ofFailed(res.getCode().name() + ":" + res.getMessage());
            }
        } catch (Throwable e) {
            throw new ArkletRuntimeException(e);
        } finally {
            BizCommandCoordinator.popBizExecution(input.getBizName(), input.getBizVersion());
        }
    }

    @Override
    public Command command() {
        return BuiltinCommand.UNINSTALL_BIZ;
    }

    @Override
    public void validate(Input input) throws CommandValidationException {
        notBlank(input.getBizName(), "bizName should not be blank");
        notBlank(input.getBizVersion(), "bizVersion should not be blank");
    }

    @Setter
    @Getter
    public static class Input extends InputMeta {
        private String bizName;
        private String bizVersion;
    }

}
