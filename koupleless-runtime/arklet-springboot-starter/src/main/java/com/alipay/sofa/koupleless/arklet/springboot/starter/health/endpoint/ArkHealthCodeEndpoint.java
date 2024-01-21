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

import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.health.HealthService;
import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health.HealthBuilder;
import com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint.model.EndpointResponseCode;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

/**
 * @author Lunarscave
 */
@Endpoint(id = "arkHealthCode")
public class ArkHealthCodeEndpoint {

    private final HealthService healthService = ArkletComponentRegistry.getHealthServiceInstance();

    @ReadOperation
    public int healthCode() {
        return ArkHealthCodeEndpoint.ofCode(new HealthBuilder().init()
            .putAllHealthData(healthService.getHealth())
            .putAllHealthData(healthService.queryModuleInfo()).build());
    }

    @ReadOperation
    public int getModuleInfoHealthCode1(@Selector String moduleType) {
        return ArkHealthCodeEndpoint.ofCode(healthService.queryModuleInfo(moduleType, null, null));
    }

    @ReadOperation
    public int getModuleInfoHealthCode2(@Selector String moduleType, @Selector String name,
                                        @Selector String version) {
        return ArkHealthCodeEndpoint.ofCode(healthService
            .queryModuleInfo(moduleType, name, version));
    }

    public static int ofCode(Health health) {
        int endpointCode;
        if (health.containsError(Constants.HEALTH_ENDPOINT_ERROR)) {
            endpointCode = EndpointResponseCode.ENDPOINT_NOT_FOUND.getCode();
        } else if (health.containsError(Constants.HEALTH_ERROR)) {
            endpointCode = EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR.getCode();
        } else if (health.containsUnhealthy(Constants.READINESS_HEALTHY)) {
            endpointCode = EndpointResponseCode.UNHEALTHY.getCode();
        } else {
            endpointCode = EndpointResponseCode.HEALTHY.getCode();
        }
        return endpointCode;
    }
}
