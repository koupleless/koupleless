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

import com.alipay.sofa.koupleless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord.Status.EXECUTING;
import static com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord.Status.FAILED;
import static com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord.Status.INITIALIZED;
import static com.alipay.sofa.koupleless.arklet.core.command.record.ProcessRecord.Status.SUCCEEDED;

/**
 * @author: yuanyuan
 * @date: 2023/8/31 3:27 下午
 */
@Getter
@Setter
public class ProcessRecord {

    private String     requestId;

    private ArkBizMeta arkBizMeta;

    private String     threadName;

    private Status     status;

    private Throwable  throwable;

    private String     errorCode;

    private String     message;

    private Date       startTime;

    private long       startTimestamp;

    private Date       endTime;

    private long       endTimestamp;

    private long       elapsedTime;

    public enum Status {

        INITIALIZED("INITIALIZED"),

        EXECUTING("EXECUTING"),

        SUCCEEDED("SUCCEEDED"),

        FAILED("FAILED");

        private String name;

        Status(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public void markFinishTime() {
        Date date = new Date();
        setEndTime(date);
        setEndTimestamp(date.getTime());
        setElapsedTime(date.getTime() - startTimestamp);
    }

    public boolean finished() {
        return SUCCEEDED.equals(getStatus()) || FAILED.equals(getStatus());
    }

    public void start() {
        if (INITIALIZED.equals(getStatus())) {
            setStatus(EXECUTING);
            ArkletLoggerFactory.getDefaultLogger().info(
                "Command execution status change: INIT -> EXECUTING");
        }
    }

    public void success() {
        if (EXECUTING.equals(getStatus())) {
            setStatus(SUCCEEDED);
            ArkletLoggerFactory.getDefaultLogger().info(
                "Command execution status change: EXECUTING -> SUCCESS");
        }
    }

    public void fail() {
        if (EXECUTING.equals(getStatus())) {
            setStatus(FAILED);
            ArkletLoggerFactory.getDefaultLogger().info(
                "Command execution status change: EXECUTING -> FAIL");
        }
    }

    public void fail(String message) {
        fail();
        setMessage(message);
    }

    public void fail(String message, Throwable throwable) {
        fail();
        setMessage(message);
        this.throwable = throwable;
    }

}
