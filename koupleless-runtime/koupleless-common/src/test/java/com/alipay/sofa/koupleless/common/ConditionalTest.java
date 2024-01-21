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
package com.alipay.sofa.koupleless.common;

import com.alipay.sofa.koupleless.common.environment.ConditionalOnMasterBiz;
import com.alipay.sofa.koupleless.common.environment.ConditionalOnNotMasterBiz;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import com.alipay.sofa.koupleless.common.exception.ErrorCodes;
import org.junit.Test;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class ConditionalTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
                                                      .withConfiguration(UserConfigurations
                                                          .of(ConditionalAutoConfiguration.class));

    @Test
    public void testConditional() {
        runner.run(context -> {
            context.assertThat().doesNotHaveBean("nonMasterBizRuntimeException");
            context.assertThat().hasBean("masterBizRuntimeException");
        });
    }

    /**
     * @author mingmen
     * @date 2023/6/14
     */
    @Configuration
    public static class ConditionalAutoConfiguration {

        @Bean
        @ConditionalOnNotMasterBiz
        public BizRuntimeException nonMasterBizRuntimeException() {
            return new BizRuntimeException(ErrorCodes.SpringContextManager.E100001, "error test.");
        }

        @Bean
        @ConditionalOnMasterBiz
        public BizRuntimeException masterBizRuntimeException() {
            return new BizRuntimeException(ErrorCodes.SpringContextManager.E100002, "error test.");
        }
    }
}
