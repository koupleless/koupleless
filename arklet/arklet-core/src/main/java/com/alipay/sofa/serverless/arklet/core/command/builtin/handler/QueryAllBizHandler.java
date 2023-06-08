package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import java.util.ArrayList;
import java.util.List;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltInCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.QueryAllBizHandler.Output;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizModel;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.common.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.OutputMeta;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class QueryAllBizHandler extends AbstractCommandHandler<InputMeta, Output> {

    @Override
    public Output handle(InputMeta queryAllBizCmd) {
        Output output = new Output();
        List<Biz> bizList = getOperationService().queryBizList();
        output.setCount(bizList.size());
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
        output.setBizList(bizModels);
        return output;
    }

    @Override
    public Command command() {
        return BuiltInCommand.QUERY_ALL_BIZ;
    }

    @Override
    public void validate(InputMeta input) throws CommandValidationException {

    }

    public static class Output extends OutputMeta {

        private int count;
        private List<BizModel> bizList;

        public Output() {
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<BizModel> getBizList() {
            return bizList;
        }

        public void setBizList(List<BizModel> bizList) {
            this.bizList = bizList;
        }
    }
}
