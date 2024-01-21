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
package com.alipay.sofa.koupleless.arklet.core.command.record;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.koupleless.arklet.core.command.meta.bizops.ArkBizMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord.Status.INITIALIZED;

/**
 * @author: yuanyuan
 * @date: 2023/8/31 3:28 下午
 */
public class ProcessRecordHolder {

    private static Map<String, ProcessRecord> processRecords = new ConcurrentHashMap<>();

    public static ProcessRecord getProcessRecord(String rid) {
        if (!StringUtils.isEmpty(rid)) {
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
        return processRecords.values().stream().filter(record -> StringUtils.isSameStr(record.getStatus().name(), status)).collect(Collectors.toList());
    }

    public static ProcessRecord createProcessRecord(String rid, ArkBizMeta arkBizMeta) {
        ProcessRecord pr = new ProcessRecord();
        pr.setRequestId(rid);
        pr.setArkBizMeta(arkBizMeta);
        pr.setStatus(INITIALIZED);
        Date date = new Date();
        pr.setStartTime(date);
        pr.setStartTimestamp(date.getTime());
        processRecords.put(rid, pr);
        return pr;
    }

}
