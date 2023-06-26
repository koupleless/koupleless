package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import java.util.Set;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.model.BizInfo;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltInCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.ArkletRuntimeException;
import com.alipay.sofa.serverless.arklet.core.common.CommandValidationException;

/**
 * @author mingmen
 * @date 2023/6/8
 */
public class InstallBizHandler extends AbstractCommandHandler<Input, Set<BizInfo>> {

    @Override
    public Output<Set<BizInfo>> handle(Input input) {
        String bizFile = input.getArkBizFilePath();
        try {
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
        return BuiltInCommand.INSTALL_BIZ;
    }

    @Override
    public void validate(Input input) throws CommandValidationException {
        notBlank(input.getArkBizFilePath(), "arkBizFilePath should not be blank");
    }

    public static class Input extends InputMeta {
        private String arkBizFilePath;

        public Input(String arkBizFilePath) {
            this.arkBizFilePath = arkBizFilePath;
        }

        public String getArkBizFilePath() {
            return arkBizFilePath;
        }

        public void setArkBizFilePath(String arkBizFilePath) {
            this.arkBizFilePath = arkBizFilePath;
        }
    }

}
