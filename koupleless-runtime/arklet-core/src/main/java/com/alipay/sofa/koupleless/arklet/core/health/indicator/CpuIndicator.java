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
package com.alipay.sofa.koupleless.arklet.core.health.indicator;

import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/**
 * @author Lunarscave
 */
public class CpuIndicator extends Indicator {

    private final CpuIndicatorHandler cpuIndicatorHandler;

    private static final String       CPU_INDICATOR_ID = Constants.CPU;

    public CpuIndicator() {
        super(CPU_INDICATOR_ID);
        cpuIndicatorHandler = new CpuIndicatorHandler();
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        Map<String, Object> cpuHealthDetails = new HashMap<>(6);

        cpuIndicatorHandler.collectTicks();
        cpuHealthDetails.put(CpuMetrics.CPU_COUNT.getId(), cpuIndicatorHandler.getCpuCount());
        cpuHealthDetails.put(CpuMetrics.CPU_TYPE.getId(), cpuIndicatorHandler.getCpuType());
        cpuHealthDetails.put(CpuMetrics.CPU_TOTAL_USED.getId(), cpuIndicatorHandler.getTotalUsed());
        cpuHealthDetails.put(CpuMetrics.CPU_USER_USED.getId(), cpuIndicatorHandler.getUserUsed());
        cpuHealthDetails.put(CpuMetrics.CPU_SYSTEM_USED.getId(),
            cpuIndicatorHandler.getSystemUsed());
        cpuHealthDetails.put(CpuMetrics.CPU_FREE.getId(), cpuIndicatorHandler.getFree());
        return cpuHealthDetails;
    }

    static class CpuIndicatorHandler {

        private final CentralProcessor cpu;

        private long[]                 prevTicks;

        private long[]                 nextTicks;

        public CpuIndicatorHandler() {
            this.cpu = new SystemInfo().getHardware().getProcessor();
            prevTicks = cpu.getSystemCpuLoadTicks();
        }

        public void collectTicks() {
            nextTicks = cpu.getSystemCpuLoadTicks();
        }

        public double getTotalUsed() {
            Set<CentralProcessor.TickType> tickTypeSet = new HashSet<CentralProcessor.TickType>(
                Arrays.asList(CentralProcessor.TickType.class.getEnumConstants()));
            double totalUsed = 0;
            for (CentralProcessor.TickType tickType : tickTypeSet) {
                totalUsed += nextTicks[tickType.getIndex()] - prevTicks[tickType.getIndex()];
            }
            return totalUsed;
        }

        public double getUserUsed() {
            return getUsed(CentralProcessor.TickType.USER);
        }

        public double getSystemUsed() {
            return getUsed(CentralProcessor.TickType.SYSTEM);
        }

        public double getFree() {
            return getUsed(CentralProcessor.TickType.IDLE);
        }

        public int getCpuCount() {
            return cpu.getLogicalProcessorCount();
        }

        public String getCpuType() {
            return cpu.getProcessorIdentifier().getName();
        }

        private double getUsed(CentralProcessor.TickType tickType) {
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

    enum CpuMetrics {

        CPU_COUNT("count"), CPU_TYPE("type"), CPU_TOTAL_USED("total used (%)"), CPU_USER_USED(
                                                                                              "user used (%)"), CPU_SYSTEM_USED(
                                                                                                                                "system used (%)"), CPU_FREE(
                                                                                                                                                             "free (%)");

        private final String id;

        CpuMetrics(String desc) {
            this.id = desc;
        }

        public String getId() {
            return id;
        };
    }
}
