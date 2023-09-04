package com.alipay.sofa.serverless.arklet.core.command.record;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.alipay.sofa.serverless.arklet.core.command.record.ProcessRecord.Status.INITIALIZED;

/**
 * @author: yuanyuan
 * @date: 2023/8/31 3:28 下午
 */
public class ProcessRecordHolder {

    private static Map<String, ProcessRecord> processRecords = new ConcurrentHashMap<>();

    public static ProcessRecord getProcessRecord(String rid) {
        if (StringUtils.isNotBlank(rid)) {
            return processRecords.get(rid);
        }
        return null;
    }

    public static List<ProcessRecord> getAllProcessRecords() {
        return new ArrayList<>(processRecords.values());
    }

    public static List<ProcessRecord> getAllExecutingProcessRecords() {
        return processRecords.values().stream().filter(record -> !record.finished()).collect(Collectors.toList());
    }

    public static List<ProcessRecord> getProcessRecordsByStatus(String status) {
        return processRecords.values().stream().filter(record -> StringUtils.equals(record.getStatus().name(), status)).collect(Collectors.toList());
    }

    public static ProcessRecord createProcessRecord(String rid) {
        ProcessRecord pr = new ProcessRecord();
        pr.setRequestId(rid);
        pr.setThreadName(Thread.currentThread().getName());
        pr.setStatus(INITIALIZED);
        pr.setStartTimestamp(System.currentTimeMillis());
        processRecords.put(rid, pr);
        return pr;
    }

}
