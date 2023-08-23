package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import java.util.Set;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.model.BizInfo;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizOps;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import lombok.Getter;
import lombok.Setter;

/**
 * @author mingmen
 * @date 2023/6/8
 */
public class InstallBizHandler extends AbstractCommandHandler<Input, Set<BizInfo>> implements ArkBizOps {

    @Override
    public Output<Set<BizInfo>> handle(Input input) {
        try {
            String bizFile = input.getArkBizFilePath();
            ClientResponse res = getOperationService().install(bizFile);
            if (ResponseCode.SUCCESS.equals(res.getCode())) {
                return Output.ofSuccess(res.getBizInfos());
            } else {
                return Output.ofFailed(res.getCode().name() + ":" + res.getMessage());
            }
        } catch (Throwable e) {
            throw new ArkletRuntimeException(e);
        }
    }

    @Override
    public Command command() {
        return BuiltinCommand.INSTALL_BIZ;
    }

    @Override
    public void validate(Input input) throws CommandValidationException {
        notBlank(input.getBizName(), "bizName should not be blank");
        notBlank(input.getBizVersion(), "bizVersion should not be blank");
        notBlank(input.getArkBizFilePath(), "arkBizFilePath should not be blank");
    }

    @Getter
    @Setter
    public static class Input extends ArkBizMeta {
        private String arkBizFilePath;
        private String remoteUrl;
    }

}
