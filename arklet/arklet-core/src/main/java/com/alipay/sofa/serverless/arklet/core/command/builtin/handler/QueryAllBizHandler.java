package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import java.util.ArrayList;
import java.util.List;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizModel;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class QueryAllBizHandler extends AbstractCommandHandler<InputMeta, List<BizModel>> {

    @Override
    public Output<List<BizModel>> handle(InputMeta inputMeta) {
        List<Biz> bizList = getOperationService().queryBizList();
        List<BizModel> bizModels = new ArrayList<>(bizList.size());
        for (Biz biz : bizList) {
            BizModel model = new BizModel();
            model.setBizName(biz.getBizName());
            model.setBizVersion(biz.getBizVersion());
            model.setBizState(biz.getBizState());
            model.setMainClass(biz.getMainClass());
            model.setWebContextPath(biz.getWebContextPath());
            bizModels.add(model);
        }
        return Output.ofSuccess(bizModels);
    }

    @Override
    public Command command() {
        return BuiltinCommand.QUERY_ALL_BIZ;
    }

    @Override
    public void validate(InputMeta input) throws CommandValidationException {

    }
}
