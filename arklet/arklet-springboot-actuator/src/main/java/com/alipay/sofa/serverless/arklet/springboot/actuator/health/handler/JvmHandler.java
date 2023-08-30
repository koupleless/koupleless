package com.alipay.sofa.serverless.arklet.springboot.actuator.health.handler;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.ConvertUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.Properties;

/**
 * @author Lunarscave
 */
public class JvmHandler {
    private final Properties properties;
    private final Runtime runtime;
    private final RuntimeMXBean runtimeMxBean;

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

    public double getduration() {
        return ConvertUtil.convertMillis2Second(new Date(runtimeMxBean.getStartTime()));
    }
}
