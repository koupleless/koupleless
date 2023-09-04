package com.alipay.sofa.serverless.arklet.core.command.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: yuanyuan
 * @date: 2023/9/1 12:02 下午
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_COUNT   = new AtomicInteger();

    /**
     * The current thread pool counter
     */
    private final AtomicInteger        threadCount  = new AtomicInteger(1);

    /**
     * Thread group
     */
    private final ThreadGroup          group;

    /**
     * Thread name prefix
     */
    private final String               namePrefix;

    /**
     * Thread daemon option
     */
    private final boolean              isDaemon;

    /**
     * The first default prefix of thread name
     */
    private final static String        FIRST_PREFIX = "ARKLET-";

    /**
     * specify the second prefix of thread name, default the thread created is non-daemon
     *
     * @param secondPrefix second prefix of thread name
     */
    public NamedThreadFactory(String secondPrefix) {
        this(secondPrefix, false);
    }

    /**
     * Construct a named thread factory
     *
     * @param secondPrefix second prefix of thread name
     * @param daemon thread daemon option
     */
    public NamedThreadFactory(String secondPrefix, boolean daemon) {
        SecurityManager sm = System.getSecurityManager();
        group = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = FIRST_PREFIX + secondPrefix + "-" + POOL_COUNT.getAndIncrement() + "-T";
        isDaemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadCount.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
