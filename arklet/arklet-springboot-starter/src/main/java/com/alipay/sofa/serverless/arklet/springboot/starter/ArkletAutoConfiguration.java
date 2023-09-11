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
package com.alipay.sofa.serverless.arklet.springboot.starter;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.springboot.starter.command.MasterBizCmdHandlerCollector;
import com.alipay.sofa.serverless.arklet.springboot.starter.environment.ConditionalOnMasterBiz;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@Configuration
public class ArkletAutoConfiguration {

    @Bean
    @ConditionalOnMasterBiz
    public ArkletComponentRegistry arkletComponentRegistry() {
        ArkletComponentRegistry registry = new ArkletComponentRegistry();
        registry.initComponents();
        return registry;
    }

    @Bean
    @ConditionalOnMasterBiz
    @DependsOn("arkletComponentRegistry")
    public MasterBizCmdHandlerCollector masterBizCmdHandlerCollector() {
        return new MasterBizCmdHandlerCollector();
    }

}
