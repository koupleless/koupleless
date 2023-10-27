package com.alipay.sofa.serverless.arklet.core.command;

import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.serverless.arklet.core.command.record.ProcessRecord;
import com.alipay.sofa.serverless.arklet.core.command.record.ProcessRecordHolder;
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
