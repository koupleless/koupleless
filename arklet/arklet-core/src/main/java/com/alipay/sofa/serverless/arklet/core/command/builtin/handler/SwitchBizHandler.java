package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltInCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.SwitchBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.common.ArkletRuntimeException;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class SwitchBizHandler extends AbstractCommandHandler<Input, Void> {

    @Override
    public Output<Void> handle(Input input) {
        try {
            ClientResponse res = getOperationService().switchBiz(input.getBizName(), input.getBizVersion());
            if (ResponseCode.SUCCESS.equals(res.getCode())) {
                return Output.ofSuccess(null);
            } else {
                return Output.ofFailed(res.getCode().name() + ":" + res.getMessage());
            }
        } catch (Throwable e) {
            throw new ArkletRuntimeException(e);
        }
    }

    @Override
    public Command command() {
        return BuiltInCommand.SWITCH_BIZ;
    }

    @Override
    public void validate(Input input) throws CommandValidationException {
        notBlank(input.getBizName(), "bizName should not be blank");
        notBlank(input.getBizVersion(), "bizVersion should not be blank");
    }

    public static class Input extends InputMeta {
        private String bizName;
        private String bizVersion;

        public Input(String bizName, String bizVersion) {
            this.bizName = bizName;
            this.bizVersion = bizVersion;
        }

        public String getBizName() {
            return bizName;
        }

        public void setBizName(String bizName) {
            this.bizName = bizName;
        }

        public String getBizVersion() {
            return bizVersion;
        }

        public void setBizVersion(String bizVersion) {
            this.bizVersion = bizVersion;
        }
    }

}
