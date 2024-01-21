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
package com.alipay.sofa.koupleless.arklet.core.health;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.health.custom.CustomBizManagerService;
import com.alipay.sofa.koupleless.arklet.core.health.custom.CustomIndicator;
import com.alipay.sofa.koupleless.arklet.core.health.custom.CustomPluginManagerService;
import com.alipay.sofa.koupleless.arklet.core.health.model.BizHealthMeta;
import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.core.health.model.PluginHealthMeta;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

public class HealthServiceTest extends BaseTest {

    private MockedStatic<ArkClient> arkClient;

    private void validateHealth(Health health, final String[] expectedMetrics) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Map<String, Object> healthData = health.getHealthData();
        for (String metric : expectedMetrics) {
            Assert.assertTrue(healthData.containsKey(metric)
                              && !((Map<?, ?>) healthData.get(metric)).isEmpty());
        }
    }

    private void validateBizListHealth(Health health, final List<Biz> expectedBizList) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Assert.assertTrue(health.getHealthData().containsKey(Constants.BIZ_LIST_INFO));

        List<?> realBizList = (List<?>) health.getHealthData().get(Constants.BIZ_LIST_INFO);
        Assert.assertEquals(realBizList.size(), expectedBizList.size());
        for (int i = 0, L = realBizList.size(); i < L; i++) {
            Object realBiz = realBizList.get(i);
            Biz expectedBiz = expectedBizList.get(i);
            Assert.assertTrue(realBiz instanceof BizHealthMeta);
            validateBiz((BizHealthMeta) realBiz, expectedBiz);
        }
    }

    private void validatePluginListHealth(Health health, final List<Plugin> expectedPluginList) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Assert.assertTrue(health.getHealthData().containsKey(Constants.PLUGIN_LIST_INFO));

        List<?> realPluginList = (List<?>) health.getHealthData().get(Constants.PLUGIN_LIST_INFO);
        Assert.assertEquals(realPluginList.size(), expectedPluginList.size());
        for (int i = 0, L = realPluginList.size(); i < L; i++) {
            Object realPlugin = realPluginList.get(i);
            Plugin expectedPlugin = expectedPluginList.get(i);
            Assert.assertTrue(realPlugin instanceof PluginHealthMeta);
            validatePlugin((PluginHealthMeta) realPlugin, expectedPlugin);
        }
    }

    private void validateHealth(Health health, final List<Biz> expectedBizList,
                                final List<Plugin> expectedPluginList, final Biz expectedMasterBiz) {
        validateBizListHealth(health, expectedBizList);
        validatePluginListHealth(health, expectedPluginList);
        Assert.assertTrue(health.getHealthData().containsKey(Constants.MASTER_BIZ_INFO));
        Object realMasterBiz = health.getHealthData().get(Constants.MASTER_BIZ_INFO);
        Assert.assertTrue(realMasterBiz instanceof BizHealthMeta);
        validateBiz((BizHealthMeta) realMasterBiz, expectedMasterBiz);
    }

    private void validateHealth(Health health, final Biz expectedBiz) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Assert.assertTrue(health.getHealthData().containsKey(Constants.BIZ_INFO));
        Object realBiz = health.getHealthData().get(Constants.BIZ_INFO);
        Assert.assertTrue(realBiz instanceof BizHealthMeta);
        validateBiz((BizHealthMeta) realBiz, expectedBiz);
    }

    private void validateHealth(Health health, final Plugin expectedPlugin) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Assert.assertTrue(health.getHealthData().containsKey(Constants.PLUGIN_INFO));
        Object realPlugin = health.getHealthData().get(Constants.PLUGIN_INFO);
        Assert.assertTrue(realPlugin instanceof PluginHealthMeta);
        validatePlugin((PluginHealthMeta) realPlugin, expectedPlugin);
    }

    private void validateHealth(Health health, final String errorCode, final String errorMessage) {
        Assert.assertTrue(health != null && !health.getHealthData().isEmpty());
        Assert.assertTrue(health.getHealthData().containsKey(errorCode));
        Assert.assertEquals(health.getHealthData().get(errorCode), errorMessage);
    }

    private void validateBiz(BizHealthMeta realBiz, final Biz expectedBiz) {
        Assert.assertEquals(realBiz.getBizName(), expectedBiz.getBizName());
        Assert.assertEquals(realBiz.getBizVersion(), expectedBiz.getBizVersion());
    }

    private void validatePlugin(PluginHealthMeta realPlugin, final Plugin expectedPlugin) {
        Assert.assertEquals(realPlugin.getPluginName(), expectedPlugin.getPluginName());
        Assert.assertEquals(realPlugin.getPluginVersion(), expectedPlugin.getVersion());
    }

    @Before
    public void initHealthService() {
        arkClient = mockStatic(ArkClient.class);
        arkClient.when(ArkClient::getBizManagerService).thenReturn(new CustomBizManagerService());
        arkClient.when(ArkClient::getPluginManagerService).thenReturn(new CustomPluginManagerService());
        arkClient.when(ArkClient::getMasterBiz).thenReturn(new CustomBizManagerService().getMasterBiz());
    }

    @Test
    public void registerCustomCIndicator() {
        healthService.registerIndicator(new CustomIndicator());
        CustomIndicator indicator = (CustomIndicator) healthService.getIndicator("custom");
        Assert.assertNotNull(indicator);
    }

    @Test
    public void testGetHealth() {
        final String[] allMetrics = new String[] { Constants.CPU, Constants.JVM };
        final String[] testMetrics = new String[] { Constants.CPU, Constants.JVM };
        final String[] errorMetrics = new String[] { "nonMetrics" };
        validateHealth(healthService.getHealth(), allMetrics);
        validateHealth(healthService.getHealth(new String[0]), allMetrics);
        validateHealth(healthService.getHealth(testMetrics[0]), new String[] { testMetrics[0] });
        validateHealth(healthService.getHealth(testMetrics), testMetrics);
        validateHealth(healthService.getHealth(errorMetrics), Constants.HEALTH_ERROR,
            "indicator not registered");
    }

    @Test
    public void testGetModuleInfo() {
        final CustomBizManagerService bizService = new CustomBizManagerService();
        final CustomPluginManagerService pluginService = new CustomPluginManagerService();
        final String bizName = "testBiz1";
        final String pluginName = "testPlugin1";
        final String bizVersion = "testBizVersion1";
        final String errorType = "errorType";
        final String errorBizName = "errorBiz";
        final String errorPluginName = "errorPlugin";

        validateHealth(healthService.queryModuleInfo(), bizService.getBizInOrder(),
            pluginService.getPluginsInOrder(), bizService.getMasterBiz());
        validateBizListHealth(healthService.queryModuleInfo(Constants.BIZ, null, null),
            bizService.getBizInOrder());
        validateBizListHealth(healthService.queryModuleInfo(Constants.BIZ, bizName, null),
            bizService.getBiz(bizName));
        validateHealth(healthService.queryModuleInfo(Constants.BIZ, bizName, bizVersion),
            bizService.getBiz(bizName, bizVersion));
        validatePluginListHealth(healthService.queryModuleInfo(Constants.PLUGIN, null, null),
            pluginService.getPluginsInOrder());
        validateHealth(healthService.queryModuleInfo(Constants.PLUGIN, pluginName, null),
            pluginService.getPluginByName(pluginName));
        validateHealth(healthService.queryModuleInfo(Constants.BIZ, errorBizName, bizVersion),
            Constants.HEALTH_ERROR, "can not find biz");
        validateHealth(healthService.queryModuleInfo(Constants.PLUGIN, errorPluginName, null),
            Constants.HEALTH_ERROR, "can not find plugin");
    }

    @Test
    public void testIndicators() {
        Assert.assertNotNull(healthService.getIndicator(Constants.CPU));
        Assert.assertNotNull(healthService.getIndicator(Constants.JVM));
    }

    @After
    public void destroyHealthService() {
        arkClient.close();
    }

}
