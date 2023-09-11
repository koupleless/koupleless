package com.alipay.sofa.serverless.arklet.core.health;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.BaseTest;
import com.alipay.sofa.serverless.arklet.core.health.custom.CustomIndicator;
import com.alipay.sofa.serverless.arklet.core.health.model.Constants;
import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class HealthTests extends BaseTest {

    private HealthService healthService;

    private void validateHealth(Health health, final String[] expectedMetrics) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Map<String, Object> healthData = health.getHealthData();
        for (String metric: expectedMetrics) {
            Assert.assertTrue(healthData.containsKey(metric) && !((Map<?, ?>) healthData.get(metric)).isEmpty());
        }
    }

    private void validateHealth(Health health, String errorCode, String errorMessage) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Assert.assertTrue(health.getHealthData().containsKey(errorCode));
        Assert.assertEquals(health.getHealthData().get(errorCode), errorMessage);
    }

    @Before
    public void initHealthService() throws IOException {
        this.healthService = ArkletComponentRegistry.getHealthServiceInstance();

//        ClassLoader cl = Thread.currentThread().getContextClassLoader();
//        URL testBiz = cl.getResource("test-biz.jar");
//        BizOperation bizOperation = new BizOperation();
//        bizOperation.setBizVersion("test version");
//        ArkClient.getBizFactoryService().createBiz(bizOperation, new File(testBiz.getFile()));
    }

    @Test
    public void registerCustomCIndicator() {
        healthService.registerIndicator(new CustomIndicator());
        CustomIndicator indicator = (CustomIndicator) healthService.getIndicator("custom");
        Assert.assertNotNull(indicator);
    }

    @Test
    public void testGetHealth() {
        final String[] allMetrics = new String[]{Constants.CPU, Constants.JVM};
        final String[] testMetrics = new String[]{Constants.CPU};
        final String[] errorMetrics = new String[]{"nonMetrics"};
        validateHealth(healthService.getHealth(), allMetrics);
        validateHealth(healthService.getHealth(new String[0]), allMetrics);
        validateHealth(healthService.getHealth(testMetrics), testMetrics);
        validateHealth(healthService.getHealth(errorMetrics), Constants.HEALTH_ERROR, "indicator not registered");
    }

    @Test
    public void testIndicators() {
        Assert.assertNotNull(healthService.getIndicator(Constants.CPU));
        Assert.assertNotNull(healthService.getIndicator(Constants.JVM));
    }
}
