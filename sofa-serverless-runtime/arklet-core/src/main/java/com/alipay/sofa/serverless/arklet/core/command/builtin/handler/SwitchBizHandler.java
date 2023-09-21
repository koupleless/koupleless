package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.SwitchBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizOps;
import com.alipay.sofa.serverless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class SwitchBizHandler extends AbstractCommandHandler<Input, ClientResponse> implements ArkBizOps {

    @Override
    public Output<ClientResponse> handle(Input input) {
        try {
            ClientResponse res = getOperationService().switchBiz(input.getBizName(), input.getBizVersion());
            if (ResponseCode.SUCCESS.equals(res.getCode())) {
                return Output.ofSuccess(res);
            } else {
                return Output.ofFailed(res, "switch biz not success!");
            }
        } catch (Throwable e) {
            throw new ArkletRuntimeException(e);
        }
    }

    @Override
    public Command command() {
        return BuiltinCommand.SWITCH_BIZ;
    }

    @Override
    public void validate(Input input) throws CommandValidationException {
        notBlank(input.getBizName(), "bizName should not be blank");
        notBlank(input.getBizVersion(), "bizVersion should not be blank");
        isTrue(!input.isAsync() || !StringUtils.isEmpty(input.getRequestId()),
            "requestId should not be blank when async is true");
    }

    public static class Input extends ArkBizMeta {
    }

}
