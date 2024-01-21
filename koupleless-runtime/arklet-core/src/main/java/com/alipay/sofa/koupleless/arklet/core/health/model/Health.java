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

import com.alipay.sofa.koupleless.arklet.core.util.AssertUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class Health {

    private final Map<String, Object> healthData;

    public Health(HealthBuilder builder) {
        this.healthData = Collections.unmodifiableMap(builder.healthData);
    }

    public Map<String, Object> getHealthData() {
        return healthData;
    }

    public boolean containsError(String errorCode) {
        return this.healthData.containsKey(errorCode);
    }

    public boolean containsUnhealthy(String healthyCode) {
        Map<String, Object> healthData = this.getHealthData();
        boolean isUnhealthy = false;
        for (String key : healthData.keySet()) {
            Object value = healthData.get(key);
            if (value instanceof Map) {
                if (((Map<?, ?>) value).containsKey("masterBizHealth")) {
                    Object health = ((Map<?, ?>) value).get("masterBizHealth");
                    if (health instanceof Map) {
                        String code = (String) ((Map<?, ?>) health).get("readinessState");
                        if (!code.equals(healthyCode)) {
                            isUnhealthy = true;
                            break;
                        }
                    }
                }
            }
        }
        return isUnhealthy;
    }

    public static class HealthBuilder {
        private final Map<String, Object> healthData = new HashMap<>();

        public Health build() {
            return new Health(this);
        }

        public HealthBuilder init() {
            this.healthData.clear();
            return this;
        }

        public HealthBuilder putAllHealthData(Health healthData) {
            this.healthData.putAll(healthData.getHealthData());
            return this;
        }

        public HealthBuilder putHealthData(String key, Object value) {
            this.healthData.put(key, value);
            return this;
        }

        public HealthBuilder putErrorData(String errorCode, String message) {
            this.healthData.clear();
            this.healthData.put(errorCode, message);
            return this;
        }

        public HealthBuilder putAllHealthData(Map<String, ?> healthData) {
            AssertUtils.assertNotNull(healthData, "health data must not null");
            this.healthData.putAll(healthData);
            return this;
        }
    }
}
