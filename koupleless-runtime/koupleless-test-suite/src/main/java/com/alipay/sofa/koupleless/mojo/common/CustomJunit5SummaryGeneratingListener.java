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
package com.alipay.sofa.koupleless.mojo.common;

import org.apache.maven.plugin.logging.Log;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

/**
 * @author CodeNoobKing
 * @date 2024/1/16
 */
public class CustomJunit5SummaryGeneratingListener extends SummaryGeneratingListener {

    private Log log;

    public CustomJunit5SummaryGeneratingListener(Log log) {
        super();
        this.log = log;
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        super.executionStarted(testIdentifier);
        if (testIdentifier.isTest()) {
            log.info(String.format("%s Starting", testIdentifier.getUniqueId()));
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        super.executionStarted(testIdentifier);
        if (testIdentifier.isTest()) {
            log.info(String.format("%s Status: %s", testIdentifier.getUniqueId(), testExecutionResult.getStatus()));
            testExecutionResult.getThrowable().ifPresent(t -> log.info(String.format("%s Error: %s", testIdentifier.getUniqueId(), t.getMessage())));
        }
    }
}
