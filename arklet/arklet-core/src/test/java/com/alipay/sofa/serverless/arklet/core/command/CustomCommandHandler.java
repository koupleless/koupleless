package com.alipay.sofa.serverless.arklet.core.command;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;

/**
 * @author mingmen
 * @date 2023/8/6
 */
public class CustomCommandHandler extends AbstractCommandHandler<Input, String> {

    @Override
    public void validate(Input input) throws CommandValidationException {
        AssertUtils.isTrue(input.id > 0, "input id should larger than 0");
        AssertUtils.isTrue(!StringUtils.isEmpty(input.userName), "input name should not be emptu");
    }

    @Override
    public Output<String> handle(Input input) {
        return Output.ofSuccess("hello world");
    }

    @Override
    public Command command() {
        return CustomCommand.HELLO;
    }
}
