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
package com.alipay.sofa.serverless.arklet.core.health.indicator;

import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import com.alipay.sofa.serverless.arklet.core.health.model.Health.HealthBuilder;

import java.util.Map;

/**
 * @author Lunarscave
 */
public abstract class ArkletBaseIndicator {

    private final String indicatorId;

    public ArkletBaseIndicator(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    protected abstract Map<String, Object> getHealthDetails();

    public String getIndicatorId() {
        return indicatorId;
    }

    public Health getHealthModel(HealthBuilder builder) {
        return builder.init().putHealthData(getIndicatorId(), getHealthDetails()).build();
    }
}
