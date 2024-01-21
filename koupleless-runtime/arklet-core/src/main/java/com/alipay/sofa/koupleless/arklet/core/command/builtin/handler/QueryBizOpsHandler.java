/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.koupleless.arklet.core.command.builtin.handler;

import com.alipay.sofa.koupleless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import com.alipay.sofa.koupleless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord;
import com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecordHolder;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import lombok.Getter;
import lombok.Setter;

import static com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand.QUERY_BIZ_OPS;

/**
 * @author: yuanyuan
 * @date: 2023/9/4 9:50 下午
 */
public class QueryBizOpsHandler extends
                               AbstractCommandHandler<QueryBizOpsHandler.Input, ProcessRecord> {

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
            return Output.ofFailed("Not found the corresponding ops record.");
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
