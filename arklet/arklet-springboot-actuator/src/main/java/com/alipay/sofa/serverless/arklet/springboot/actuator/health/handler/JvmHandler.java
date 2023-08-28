package com.alipay.sofa.serverless.arklet.springboot.actuator.health.handler;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.Properties;

public class JvmHandler {
    private Properties properties;
    private Runtime runtime;
    private RuntimeMXBean runtimeMxBean;

    public  JvmHandler() {
        this.properties = System.getProperties();
        this.runtime = Runtime.getRuntime();
        this.runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    }

    public String getJvmVersion() {
        return this.properties.getProperty("java.version");
    }

    public String getJavaHome() {
        return this.properties.getProperty("java.home");
    }

    public long getTotalMemory() {
        return this.runtime.totalMemory();
    }

    public long getMaxMemory() {
        return this.runtime.maxMemory();
    }

    public long getFreeMemory() {
        return this.runtime.freeMemory();
    }

    public Date getduration() {
        return new Date(runtimeMxBean.getStartTime());
    }
}
