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
package com.alipay.sofa.koupleless.arklet.springboot.starter.health.extension.indicator;

import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.health.HealthService;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.springboot.starter.SpringbootBaseTest;
import com.alipay.sofa.koupleless.arklet.springboot.starter.common.SpringbootUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.availability.ApplicationAvailability;

import java.util.Map;

/**
 * @author Lunarscave
 */
public class MasterBizHealthIndicatorTest extends SpringbootBaseTest {

    private static MasterBizHealthIndicator indicator;

    @BeforeClass
    public static void before() {
        indicator = new MasterBizHealthIndicator();
        indicator.setApplicationAvailability(SpringbootUtil.getBean(ApplicationAvailability.class));
    }

    @Test
    public void testMasterBizIndicator() {
        Health health = indicator.getHealthModel(new Health.HealthBuilder());
        Assert.assertNotNull(health);
        Assert.assertTrue(health.getHealthData().get(indicator.getIndicatorId()) instanceof Map);
        Map<?, ?> healthData = (Map<?, ?>) health.getHealthData().get(indicator.getIndicatorId());
        Assert.assertTrue(!healthData.isEmpty() && healthData.containsKey("readinessState"));
    }

    @Test
    public void testRegisterMasterBizIndicator() {
        HealthService healthService = ArkletComponentRegistry.getHealthServiceInstance();
        Assert.assertNotNull(healthService.getIndicator(indicator.getIndicatorId()));
    }

}
