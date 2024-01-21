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
import com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint.model.EndpointResponseCode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Lunarscave
 */
public class ArkHealthCodeEndpointTest extends SpringbootBaseTest {
    private static ArkHealthCodeEndpoint arkHealthCodeEndpoint;
    private static String                bizName;
    private static String                bizVersion;

    @BeforeClass
    public static void initHealthService() {
        arkHealthCodeEndpoint = new ArkHealthCodeEndpoint();
        bizName = SpringbootUtil.getProperty("spring.application.name");
        bizVersion = "1.0.0";
    }

    @Test
    public void testHealthCode() {
        Assert.assertEquals(arkHealthCodeEndpoint.healthCode(),
            EndpointResponseCode.HEALTHY.getCode());
    }

    @Test
    public void testGetModuleInfoHealthCode1_BizType() {
        Assert.assertEquals(arkHealthCodeEndpoint.getModuleInfoHealthCode1(Constants.BIZ),
            EndpointResponseCode.HEALTHY.getCode());
    }

    @Test
    public void testGetModuleInfoHealthCode1_PluginType() {
        Assert.assertEquals(arkHealthCodeEndpoint.getModuleInfoHealthCode1(Constants.PLUGIN),
            EndpointResponseCode.HEALTHY.getCode());
    }

    @Test
    public void testGetModuleInfoHealthCode1_NonType() {
        final String nonType = "non";
        Assert.assertEquals(arkHealthCodeEndpoint.getModuleInfoHealthCode1(nonType),
            EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR.getCode());
    }

    @Test
    public void testGetModuleInfoHealthCode2_BizSuccess() {
        Assert.assertEquals(
            arkHealthCodeEndpoint.getModuleInfoHealthCode2(Constants.BIZ, bizName, bizVersion),
            EndpointResponseCode.HEALTHY.getCode());
    }

    @Test
    public void testGetModuleInfo2_BizFailure() {
        final String nonBizName = "non";
        Assert.assertEquals(
            arkHealthCodeEndpoint.getModuleInfoHealthCode2(Constants.BIZ, nonBizName, bizVersion),
            EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR.getCode());
    }

    @Test
    public void testGetModuleInfo2_PluginFailure() {
        final String nonPluginName = "non";
        final String nonPluginVersion = "x.x.x";
        Assert.assertEquals(arkHealthCodeEndpoint.getModuleInfoHealthCode2(Constants.BIZ,
            nonPluginName, nonPluginVersion), EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR
            .getCode());
    }
}
