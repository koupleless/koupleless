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
package com.alipay.sofa.koupleless.test.suite.spring.multi;

import com.alipay.sofa.koupleless.test.suite.spring.mock.common.HelloService;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBaseSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessMultiSpringTestConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
public class KouplelessTestMultiSSpringApplicationTest {

    @Test
    public void testMultiApplicationLaunched() throws Throwable {
        KouplelessBaseSpringTestConfig baseConfig = KouplelessBaseSpringTestConfig.builder()
            .packageName("com.alipay.sofa.koupleless.test.suite.spring.mock.base").build();

        List<KouplelessBizSpringTestConfig> bizConfigs = new ArrayList<>();
        bizConfigs.add(KouplelessBizSpringTestConfig.builder()
            .packageName("com.alipay.sofa.koupleless.test.suite.spring.mock.biz").bizName("biz0")
            .build());

        KouplelessTestMultiSpringApplication application = new KouplelessTestMultiSpringApplication(
            KouplelessMultiSpringTestConfig.builder().baseConfig(baseConfig).bizConfigs(bizConfigs)
                .build());

        application.run();
        Thread.sleep(1_000);

        HelloService sampleBaseService = application.getBaseApplication().getApplicationContext()
            .getBean(HelloService.class);

        Assert.assertEquals(Thread.currentThread().getContextClassLoader().getClass().getName(),
            sampleBaseService.helloWorld());

        HelloService sampleBizService = application.getBizApplication("biz0")
            .getApplicationContext().getBean(HelloService.class);

        Assert.assertEquals("com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader",
            sampleBizService.helloWorld());
    }
}
