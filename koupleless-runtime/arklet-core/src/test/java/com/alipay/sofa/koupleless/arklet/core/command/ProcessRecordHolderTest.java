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
package com.alipay.sofa.koupleless.arklet.core.command;

import com.alipay.sofa.koupleless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord;
import com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecordHolder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author: yuanyuan
 * @date: 2023/10/27 6:27 下午
 */
public class ProcessRecordHolderTest {

    @Test
    public void test() {
        ProcessRecordHolder.createProcessRecord("rid", new ArkBizMeta());
        ProcessRecord processRecord = ProcessRecordHolder.getProcessRecord("rid");
        Assert.assertNotNull(processRecord);
        Assert.assertNotEquals(0, ProcessRecordHolder.getAllProcessRecords().size());
        Assert.assertEquals(1, ProcessRecordHolder.getAllExecutingProcessRecords().size());
        Assert.assertEquals(1, ProcessRecordHolder.getProcessRecordsByStatus("INITIALIZED").size());
    }
}
