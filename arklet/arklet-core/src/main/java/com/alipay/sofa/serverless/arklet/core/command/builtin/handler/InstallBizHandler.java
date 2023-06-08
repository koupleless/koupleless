package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltInCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler.Input;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler.Output;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.common.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.OutputMeta;
import com.alipay.sofa.serverless.arklet.core.common.ArkletRuntimeException;

/**
 * @author mingmen
 * @date 2023/6/8
 */
public class InstallBizHandler extends AbstractCommandHandler<Input, Output> {

    @Override
    public Output handle(Input input) {
        String bizFile = input.getArkBizFilePath();
        try {
            ResponseCode code = getOperationService().install(bizFile);
            return new Output(code.name());
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

    public static class Output extends OutputMeta {
        private String res;

        public Output(String res) {
            this.res = res;
        }

        public String getRes() {
            return res;
        }

        public void setRes(String res) {
            this.res = res;
        }
    }

}
