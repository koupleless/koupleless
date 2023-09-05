package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.command.record.ProcessRecord;
import com.alipay.sofa.serverless.arklet.core.command.record.ProcessRecordHolder;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import lombok.Getter;
import lombok.Setter;

import static com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand.QUERY_BIZ_OPS;

/**
 * @author: yuanyuan
 * @date: 2023/9/4 9:50 下午
 */
public class QueryBizOpsHandler extends AbstractCommandHandler<QueryBizOpsHandler.Input, ProcessRecord>  {

    @Override
    public void validate(Input input) throws CommandValidationException {
        notNull(input, "request is null");
        notBlank(input.getRequestId(), "requestId is blank");
    }

    @Override
    public Output<ProcessRecord> handle(Input input) {
        String requestId = input.getRequestId();
        ProcessRecord processRecord = ProcessRecordHolder.getProcessRecord(requestId);
        if (processRecord == null) {
            Output.ofFailed("Not found the corresponding ops record.");
        }
        return Output.ofSuccess(processRecord);
    }

    @Override
    public Command command() {
        return QUERY_BIZ_OPS;
    }

    @Getter
    @Setter
    public static class Input extends InputMeta {
        private String requestId;
    }
}
