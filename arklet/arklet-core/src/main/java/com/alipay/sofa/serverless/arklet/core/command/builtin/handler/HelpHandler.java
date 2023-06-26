package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import java.util.ArrayList;
import java.util.List;

import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltInCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.CommandModel;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.CommandValidationException;

/**
 * @author mingmen
 * @date 2023/6/14
 */

@SuppressWarnings("rawtypes")
public class HelpHandler extends AbstractCommandHandler<InputMeta, List> {

    @Override
    public Output<List> handle(InputMeta inputMeta) {
        List<AbstractCommandHandler> list = getCommandService().listAllHandlers();
        List<CommandModel> models = new ArrayList<>(list.size());
        for (AbstractCommandHandler handler : list) {
            models.add(new CommandModel(handler.command().getId(), handler.command().getDesc(), handler.command().getSample()));
        }
        return Output.ofSuccess(models);
    }

    @Override
    public Command command() {
        return BuiltInCommand.HELP;
    }

    @Override
    public void validate(InputMeta input) throws CommandValidationException {

    }

}
