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
package com.alipay.sofa.koupleless.arklet.core.command.executor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: yuanyuan
 * @date: 2023/8/31 4:12 下午
 */
public class ExecutorServiceManager {

    private static ThreadPoolExecutor ARK_BIZ_OPS_EXECUTOR = new ThreadPoolExecutor(
                                                               20,
                                                               50,
                                                               30,
                                                               TimeUnit.SECONDS,
                                                               new ArrayBlockingQueue<>(100),
                                                               new NamedThreadFactory("ark-biz-ops"),
                                                               new ThreadPoolExecutor.CallerRunsPolicy());

    public static ThreadPoolExecutor getArkBizOpsExecutor() {
        return ARK_BIZ_OPS_EXECUTOR;
    }
}
