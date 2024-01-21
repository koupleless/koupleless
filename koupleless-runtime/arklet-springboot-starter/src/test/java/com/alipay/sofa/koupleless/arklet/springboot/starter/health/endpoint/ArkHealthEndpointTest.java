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
package com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint;

import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import com.alipay.sofa.koupleless.arklet.springboot.starter.SpringbootBaseTest;
import com.alipay.sofa.koupleless.arklet.springboot.starter.common.SpringbootUtil;
import com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint.model.EndpointResponse;
import com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint.model.EndpointResponseCode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

/**
 * @author Lunarscave
 */
public class ArkHealthEndpointTest extends SpringbootBaseTest {

    private static ArkHealthEndpoint arkHealthEndpoint;
    private static String            bizName;
    private static String            bizVersion;
    private final String[]           indicatorIds = { Constants.CPU, Constants.JVM,
            Constants.MASTER_BIZ_HEALTH, Constants.MASTER_BIZ_INFO, Constants.BIZ_LIST_INFO,
            Constants.PLUGIN_LIST_INFO           };

    private void testEndpointHeader(EndpointResponse<Map<String, Object>> response) {
        Assert.assertNotNull(response);
        Assert.assertTrue(EndpointResponseCode.existCode(response.getCode()));
        Assert.assertNotNull(response.getCodeType());
        Map<String, Object> healthData = response.getData();
        Assert.assertTrue(healthData != null && !healthData.isEmpty());
    }

    private void testEndpointHeader(EndpointResponse<Map<String, Object>> response, int code) {
        testEndpointHeader(response);
        Assert.assertEquals(response.getCode(), code);
        Assert.assertEquals(response.getCodeType(),
            EndpointResponseCode.getEndpointResponseCode(code));
        Assert
            .assertEquals(response.isHealthy(), EndpointResponseCode.HEALTHY
                .equals(EndpointResponseCode.getEndpointResponseCode(code)));
    }

    private void testEndpointHeader(EndpointResponse<Map<String, Object>> response,
                                    EndpointResponseCode code) {
        testEndpointHeader(response);
        Assert.assertEquals(response.getCode(), code.getCode());
        Assert.assertEquals(response.getCodeType(),
            EndpointResponseCode.getEndpointResponseCode(code.getCode()));
        Assert.assertEquals(response.isHealthy(), EndpointResponseCode.HEALTHY.equals(code));
    }

    private void testEndpointData(EndpointResponse<Map<String, Object>> response,
                                  String healthDataKey) {
        Map<String, Object> healthData = response.getData();
        Assert.assertTrue(healthData.get(healthDataKey) != null);
        if (healthData.get(healthDataKey) instanceof Map) {
            Map<?, ?> healthMap = (Map<?, ?>) healthData.get(healthDataKey);
            Assert.assertTrue(!healthMap.isEmpty());
        }
    }

    private void testEndpointData(EndpointResponse<Map<String, Object>> response,
                                  String[] healthDataKeys) {
        for (String healthDataKey : healthDataKeys) {
            testEndpointData(response, healthDataKey);
        }
    }

    private void testEndpointData(EndpointResponse<Map<String, Object>> response, String errorKey,
                                  String errorMessage) {
        testEndpointData(response, errorKey);
        Map<String, Object> healthData = response.getData();
        Assert.assertEquals(healthData.get(errorKey), errorMessage);
    }

    @BeforeClass
    public static void initHealthService() {
        arkHealthEndpoint = new ArkHealthEndpoint();
        bizName = SpringbootUtil.getProperty("spring.application.name");
        bizVersion = "1.0.0";
    }

    @Test
    public void testGetHealth() {
        EndpointResponse<Map<String, Object>> response = arkHealthEndpoint.getHealth();
        testEndpointHeader(response);
        testEndpointData(response, indicatorIds);
    }

    @Test
    public void testGetModuleInfo1_BizType() {
        EndpointResponse<Map<String, Object>> response = arkHealthEndpoint
            .getModuleInfo1(Constants.BIZ);
        testEndpointHeader(response);
        testEndpointData(response, Constants.BIZ_LIST_INFO);
    }

    @Test
    public void testGetModuleInfo1_PluginType() {
        EndpointResponse<Map<String, Object>> response = arkHealthEndpoint
            .getModuleInfo1(Constants.PLUGIN);
        testEndpointHeader(response);
        testEndpointData(response, Constants.PLUGIN_LIST_INFO);
    }

    @Test
    public void testGetModuleInfo1_NonType() {
        final String nonType = "non";
        EndpointResponse<Map<String, Object>> response = arkHealthEndpoint.getModuleInfo1(nonType);
        testEndpointHeader(response, EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR);
        testEndpointData(response, Constants.HEALTH_ERROR,
            String.format("illegal type: %s", nonType));
    }

    @Test
    public void testGetModuleInfo2_BizSuccess() {
        EndpointResponse<Map<String, Object>> response = arkHealthEndpoint.getModuleInfo2(
            Constants.BIZ, bizName, bizVersion);
        testEndpointHeader(response);
        testEndpointData(response, Constants.BIZ_INFO);
    }

    @Test
    public void testGetModuleInfo2_BizFailure() {
        final String nonBizName = "non";
        EndpointResponse<Map<String, Object>> response = arkHealthEndpoint.getModuleInfo2(
            Constants.BIZ, nonBizName, bizVersion);
        testEndpointHeader(response, EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR);
        testEndpointData(response, Constants.HEALTH_ERROR, "can not find biz");
    }

    @Test
    public void testGetModuleInfo2_PluginFailure() {
        final String nonPluginName = "non";
        final String nonPluginVersion = "x.x.x";
        EndpointResponse<Map<String, Object>> response = arkHealthEndpoint.getModuleInfo2(
            Constants.PLUGIN, nonPluginName, nonPluginVersion);
        testEndpointHeader(response, EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR);
        testEndpointData(response, Constants.HEALTH_ERROR, "can not find plugin");
    }
}
