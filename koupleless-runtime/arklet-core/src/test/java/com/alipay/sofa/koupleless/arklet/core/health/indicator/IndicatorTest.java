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

import com.alipay.sofa.koupleless.arklet.core.health.model.Health.HealthBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class IndicatorTest {

    @Test
    public void testCpuIndicator() {
        Indicator indicator = new CpuIndicator();
        final String[] indicatorMetrics = new String[] { "count", "type", "total used (%)",
                "user used (%)", "system used (%)", "free (%)" };
        final String indicatorId = "cpu";
        Map<String, Object> indicatorData = indicator.getHealthDetails();
        Assert.assertEquals(indicator.getIndicatorId(), indicatorId);
        Assert.assertNotNull(indicatorData);
        Assert.assertNotNull(indicator.getHealthModel(new HealthBuilder()));
        for (String indicatorMetric : indicatorMetrics) {
            Assert.assertNotNull(indicatorData.get(indicatorMetric));
        }
    }

    @Test
    public void testJvmIndicator() {
        Indicator indicator = new JvmIndicator();
        final String[] indicatorMetrics = new String[] { "java version", "java home",
                "total memory(M)", "max memory(M)", "free memory(M)", "run time(s)",
                "init heap memory(M)", "used heap memory(M)", "committed heap memory(M)",
                "max heap memory(M)", "init non heap memory(M)", "used non heap memory(M)",
                "committed non heap memory(M)", "max non heap memory(M)", "loaded class count",
                "unload class count", "total class count" };
        final String indicatorId = "jvm";
        Map<String, Object> indicatorData = indicator.getHealthDetails();
        Assert.assertEquals(indicator.getIndicatorId(), indicatorId);
        Assert.assertNotNull(indicatorData);
        Assert.assertNotNull(indicator.getHealthModel(new HealthBuilder()));
        for (String indicatorMetric : indicatorMetrics) {
            Assert.assertNotNull(indicatorData.get(indicatorMetric));
        }
    }
}
