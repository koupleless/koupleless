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
package com.alipay.sofa.koupleless.plugin;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.common.service.ArkAutowiredBeanPostProcessor;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: yuanyuan
 * @date: 2023/10/27 4:47 下午
 */
public class BaseRuntimeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                                                             .withConfiguration(AutoConfigurations
                                                                 .of(BaseRuntimeAutoConfiguration.class));

    @Before
    public void prepare() {
        Biz biz = mock(Biz.class);
        when(biz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
        BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(biz);
        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntimeContext);
    }

    @Test
    public void test() {
        contextRunner.run(context -> {
            Assertions.assertThat(context).hasSingleBean(BizRuntimeContext.class);
            Assertions.assertThat(context).doesNotHaveBean(ArkAutowiredBeanPostProcessor.class);
        });
    }
}
