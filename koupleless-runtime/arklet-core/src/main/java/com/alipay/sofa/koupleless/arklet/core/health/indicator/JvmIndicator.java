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
import com.alipay.sofa.koupleless.arklet.core.util.ConvertUtils;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Lunarscave
 */
public class JvmIndicator extends Indicator {

    private final JvmIndicatorHandler jvmIndicatorHandler;

    private static final String       JVM_INDICATOR_ID = Constants.JVM;

    public JvmIndicator() {
        super(JVM_INDICATOR_ID);
        jvmIndicatorHandler = new JvmIndicatorHandler();
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        Map<String, Object> jvmHealthDetails = new HashMap<>(6);

        jvmIndicatorHandler.updateHandler();
        jvmHealthDetails.put(JvmMetrics.JAVA_VERSION.getId(), jvmIndicatorHandler.getJvmVersion());
        jvmHealthDetails.put(JvmMetrics.JAVA_HOME.getId(), jvmIndicatorHandler.getJavaHome());
        jvmHealthDetails.put(JvmMetrics.JAVA_TOTAL_MEMORY.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getTotalMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_MAX_MEMORY.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getMaxMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_FREE_MEMORY.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getFreeMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_INIT_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getInitHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_USED_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getUsedHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_COMMITTED_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getCommittedHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_MAX_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getMaxHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_INIT_NON_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getInitNonHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_USED_NON_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getUsedNonHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_COMMITTED_NON_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getCommittedNonHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_MAX_NON_HEAP.getId(),
            ConvertUtils.bytes2Megabyte(jvmIndicatorHandler.getMaxNonHeapMemory()));
        jvmHealthDetails.put(JvmMetrics.JAVA_LOADED_CLASS_COUNT.getId(),
            jvmIndicatorHandler.getLoadedClassCount());
        jvmHealthDetails.put(JvmMetrics.JAVA_UNLOAD_CLASS_COUNT.getId(),
            jvmIndicatorHandler.getUnloadClassCount());
        jvmHealthDetails.put(JvmMetrics.JAVA_TOTAL_CLASS_COUNT.getId(),
            jvmIndicatorHandler.getTotalClassCount());
        jvmHealthDetails.put(JvmMetrics.JAVA_RUN_TIMES.getId(), jvmIndicatorHandler.getDuration());
        return jvmHealthDetails;
    }

    static class JvmIndicatorHandler {

        private Properties         properties;

        private Runtime            runtime;

        private RuntimeMXBean      runtimeMxBean;

        private MemoryUsage        heapMemoryUsed;

        private MemoryUsage        nonHeapMemoryUsed;

        private ClassLoadingMXBean classLoadingMxBean;

        public JvmIndicatorHandler() {
            updateHandler();
        }

        public void updateHandler() {
            this.properties = System.getProperties();
            this.runtime = Runtime.getRuntime();
            this.runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            this.heapMemoryUsed = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            this.nonHeapMemoryUsed = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
            this.classLoadingMxBean = ManagementFactory.getClassLoadingMXBean();
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

        public double getDuration() {
            return ConvertUtils.getDurationSecond(new Date(runtimeMxBean.getStartTime()));
        }

        public long getInitHeapMemory() {
            return this.heapMemoryUsed.getInit();
        }

        public long getUsedHeapMemory() {
            return this.heapMemoryUsed.getUsed();
        }

        public long getCommittedHeapMemory() {
            return this.heapMemoryUsed.getCommitted();
        }

        public long getMaxHeapMemory() {
            return this.heapMemoryUsed.getMax();
        }

        public long getInitNonHeapMemory() {
            return this.nonHeapMemoryUsed.getInit();
        }

        public long getUsedNonHeapMemory() {
            return this.nonHeapMemoryUsed.getUsed();
        }

        public long getCommittedNonHeapMemory() {
            return this.nonHeapMemoryUsed.getCommitted();
        }

        public long getMaxNonHeapMemory() {
            return this.nonHeapMemoryUsed.getMax();
        }

        public long getLoadedClassCount() {
            return this.classLoadingMxBean.getLoadedClassCount();
        }

        public long getUnloadClassCount() {
            return this.classLoadingMxBean.getUnloadedClassCount();
        }

        public long getTotalClassCount() {
            return this.classLoadingMxBean.getTotalLoadedClassCount();
        }

    }

    public enum JvmMetrics {

        JAVA_VERSION("java version"), JAVA_HOME("java home"), JAVA_TOTAL_MEMORY("total memory(M)"), JAVA_MAX_MEMORY(
                                                                                                                    "max memory(M)"), JAVA_FREE_MEMORY(
                                                                                                                                                       "free memory(M)"), JAVA_RUN_TIMES(
                                                                                                                                                                                         "run time(s)"), JAVA_INIT_HEAP(
                                                                                                                                                                                                                        "init heap memory(M)"), JAVA_USED_HEAP(
                                                                                                                                                                                                                                                               "used heap memory(M)"), JAVA_COMMITTED_HEAP(
                                                                                                                                                                                                                                                                                                           "committed heap memory(M)"), JAVA_MAX_HEAP(
                                                                                                                                                                                                                                                                                                                                                      "max heap memory(M)"), JAVA_INIT_NON_HEAP(
                                                                                                                                                                                                                                                                                                                                                                                                "init non heap memory(M)"), JAVA_USED_NON_HEAP(
                                                                                                                                                                                                                                                                                                                                                                                                                                               "used non heap memory(M)"), JAVA_COMMITTED_NON_HEAP(
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   "committed non heap memory(M)"), JAVA_MAX_NON_HEAP(
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      "max non heap memory(M)"), JAVA_LOADED_CLASS_COUNT(
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         "loaded class count"), JAVA_UNLOAD_CLASS_COUNT(
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        "unload class count"), JAVA_TOTAL_CLASS_COUNT(
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      "total class count");

        private final String id;

        JvmMetrics(String desc) {
            this.id = desc;
        }

        public String getId() {
            return id;
        };
    }
}
