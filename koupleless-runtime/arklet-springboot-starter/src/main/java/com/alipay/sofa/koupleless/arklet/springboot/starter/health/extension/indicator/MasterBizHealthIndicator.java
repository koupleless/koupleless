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

import com.alipay.sofa.koupleless.arklet.core.health.indicator.Indicator;
import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import com.alipay.sofa.koupleless.arklet.core.util.AssertUtils;
import org.springframework.boot.availability.ApplicationAvailability;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class MasterBizHealthIndicator extends Indicator {

    private ApplicationAvailability applicationAvailability;

    private final static String     MASTER_BIZ_HEALTH_INDICATOR_ID = Constants.MASTER_BIZ_HEALTH;

    public MasterBizHealthIndicator() {
        super(MASTER_BIZ_HEALTH_INDICATOR_ID);
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        AssertUtils.assertNotNull(applicationAvailability, "applicationAvailability must not null");
        Map<String, Object> masterBizHealthDetails = new HashMap<>(1);
        masterBizHealthDetails.put(MasterBizHealthMetrics.READINESS_STATE.getId(),
            applicationAvailability.getReadinessState());
        return masterBizHealthDetails;
    }

    public void setApplicationAvailability(ApplicationAvailability applicationAvailability) {
        this.applicationAvailability = applicationAvailability;
    }

    enum MasterBizHealthMetrics {

        READINESS_STATE("readinessState");

        private final String id;

        MasterBizHealthMetrics(String desc) {
            this.id = desc;
        }

        public String getId() {
            return id;
        };
    }
}
