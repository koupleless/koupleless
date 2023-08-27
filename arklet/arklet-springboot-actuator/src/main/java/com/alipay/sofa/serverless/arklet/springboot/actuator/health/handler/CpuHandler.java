package com.alipay.sofa.serverless.arklet.springboot.actuator.health.handler;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.util.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lunarscave
 */
public class CpuHandler {

    private static final int WAIT_SECOND = 500;

    private final CentralProcessor cpu;

    private long[] prevTicks;

    private long[] nextTicks;

    public CpuHandler() {
        this.cpu = new SystemInfo().getHardware().getProcessor();
    }

    public void collectTicks() {
        prevTicks = cpu.getSystemCpuLoadTicks();
        Util.sleep(WAIT_SECOND);
        nextTicks = cpu.getSystemCpuLoadTicks();
    }

    public double getTotalUsed() {
        Set<TickType> tickTypeSet = new HashSet<TickType>() {{
            addAll(Arrays.asList(TickType.class.getEnumConstants()));
        }};
        double totalUsed = 0;
        for (TickType tickType : tickTypeSet) {
            totalUsed += nextTicks[tickType.getIndex()] - prevTicks[tickType.getIndex()];
        }
        return totalUsed;
    }

    public double getUserUsed() {
        return getUsed(TickType.USER);
    }

    public double getSystemUsed() {
        return getUsed(TickType.SYSTEM);
    }

    public double getFree() {
        return getUsed(TickType.IDLE);
    }

    public int getCpuCount() {
        return cpu.getLogicalProcessorCount();
    }

    public String getCpuType() {
        return cpu.getProcessorIdentifier().getName();
    }

    private double getUsed(TickType tickType) {
        double totalUsed = getTotalUsed();
        double used = nextTicks[tickType.getIndex()] - prevTicks[tickType.getIndex()];
        if (totalUsed == 0 || used < 0) {
            used = 0;
        } else {
            used = 100d * used / totalUsed;
        }
        return used;
    }
}
