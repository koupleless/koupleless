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
import com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint.model.EndpointResponse;
import com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint.model.EndpointResponseCode;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Map;

/**
 * @author Lunarscave
 */
@Endpoint(id = "arkHealth")
public class ArkHealthEndpoint {

    private final HealthService healthService = ArkletComponentRegistry.getHealthServiceInstance();

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getHealth() {
        return ArkHealthEndpoint.ofResponse(new HealthBuilder().init()
            .putAllHealthData(healthService.getHealth())
            .putAllHealthData(healthService.queryModuleInfo()).build());
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getModuleInfo1(@Selector String moduleType) {
        return ArkHealthEndpoint.ofResponse(healthService.queryModuleInfo(moduleType, null, null));
    }

    @ReadOperation
    public EndpointResponse<Map<String, Object>> getModuleInfo2(@Selector String moduleType,
                                                                @Selector String name,
                                                                @Selector String version) {
        return ArkHealthEndpoint.ofResponse(healthService
            .queryModuleInfo(moduleType, name, version));
    }

    private static EndpointResponse<Map<String, Object>> ofResponse(Health health) {
        Map<String, Object> healthData = health.getHealthData();
        EndpointResponse<Map<String, Object>> endpointResponse;
        if (health.containsError(Constants.HEALTH_ENDPOINT_ERROR)) {
            endpointResponse = EndpointResponse.ofFailed(EndpointResponseCode.ENDPOINT_NOT_FOUND,
                healthData);
        } else if (health.containsError(Constants.HEALTH_ERROR)) {
            endpointResponse = EndpointResponse.ofFailed(
                EndpointResponseCode.ENDPOINT_PROCESS_INTERNAL_ERROR, healthData);
        } else if (health.containsUnhealthy(Constants.READINESS_HEALTHY)) {
            endpointResponse = EndpointResponse
                .ofFailed(EndpointResponseCode.UNHEALTHY, healthData);
        } else {
            endpointResponse = EndpointResponse.ofSuccess(healthData);
        }
        return endpointResponse;
    }
}
