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
package com.alipay.sofa.koupleless.arklet.core.health.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lunarscave
 */
public class HealthTests {
    private final String        healthKey   = "health key";
    private final String        healthValue = "health value";

    private Map<String, Object> healthData;

    @Before
    public void init() {
        healthData = new HashMap<>();
        healthData.put(healthKey, healthValue);
    }

    @Test
    public void testPutAllHealthData() {
        Health.HealthBuilder builder = new Health.HealthBuilder();
        Health health = builder.putAllHealthData(healthData).build();
        Assert.assertTrue(health.getHealthData().containsKey(healthKey));
        Assert.assertEquals(health.getHealthData().get(healthKey), healthValue);
    }

    @Test
    public void testContainsUnhealthy_Unhealthy() {
        Map<String, Object> masterBizHealth = new HashMap<>();
        masterBizHealth.put("readinessState", "unhealthy");
        Map<String, Object> healthDataInfo = new HashMap<>();
        healthDataInfo.put(Constants.MASTER_BIZ_HEALTH, masterBizHealth);
        healthData.put("health data info", healthDataInfo);
        Health.HealthBuilder builder = new Health.HealthBuilder();
        Health health = builder.putAllHealthData(healthData).build();
        Assert.assertTrue(health.containsUnhealthy(Constants.READINESS_HEALTHY));
    }
}
