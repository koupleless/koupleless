package com.alipay.sofa.serverless.arklet.core.command.executor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: yuanyuan
 * @date: 2023/8/31 4:12 下午
 */
public class ExecutorServiceManager {

    private static ThreadPoolExecutor       ARK_BIZ_OPS_EXECUTOR            = new ThreadPoolExecutor(
            20,
            50,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(
                    100),
            new NamedThreadFactory(
                    "ark-biz-ops"),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static ThreadPoolExecutor getArkBizOpsExecutor() {
        return ARK_BIZ_OPS_EXECUTOR;
    }
}
