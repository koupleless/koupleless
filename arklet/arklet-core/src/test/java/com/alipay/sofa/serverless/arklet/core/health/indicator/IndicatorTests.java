package com.alipay.sofa.serverless.arklet.core.health.indicator;

import com.alipay.sofa.serverless.arklet.core.health.model.Health.HealthBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class IndicatorTests {

    @Test
    public void testCpuIndicator() {
        ArkletBaseIndicator indicator = new CpuIndicator();
        final String[] indicatorMetrics = new String[]{"count", "type", "total used (%)", "user used (%)", "system used (%)", "free (%)"};
        final String indicatorId = "cpu";
        Map<String, Object> indicatorData = indicator.getHealthDetails();
        Assert.assertEquals(indicator.getIndicatorId(), indicatorId);
        Assert.assertNotNull(indicatorData);
        Assert.assertNotNull(indicator.getHealthModel(new HealthBuilder()));
        for (String indicatorMetric: indicatorMetrics) {
            Assert.assertNotNull(indicatorData.get(indicatorMetric));
        }
    }

    @Test
    public void testJvmIndicator() {
        ArkletBaseIndicator indicator = new JvmIndicator();
        final String[] indicatorMetrics = new String[]{"java version", "java home", "total memory(M)", "max memory(M)",
                "free memory(M)", "run time(s)", "init heap memory(M)", "used heap memory(M)", "committed heap memory(M)",
                "max heap memory(M)", "init non heap memory(M)", "used non heap memory(M)", "committed non heap memory(M)",
                "max non heap memory(M)", "loaded class count", "unload class count", "total class count"};
        final String indicatorId = "jvm";
        Map<String, Object> indicatorData = indicator.getHealthDetails();
        Assert.assertEquals(indicator.getIndicatorId(), indicatorId);
        Assert.assertNotNull(indicatorData);
        Assert.assertNotNull(indicator.getHealthModel(new HealthBuilder()));
        for (String indicatorMetric: indicatorMetrics) {
            Assert.assertNotNull(indicatorData.get(indicatorMetric));
        }
    }
}