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

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */

import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBootstrap;
import com.alipay.sofa.koupleless.test.suite.spring.base.KouplelessBaseSpringTestApplication;
import com.alipay.sofa.koupleless.test.suite.spring.biz.KouplelessBizSpringTestApplication;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessMultiSpringTestConfig;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CodeNoobKing
 * @date 2024/3/7
 */
public class KouplelessTestMultiSpringApplication {

    @Getter
    private KouplelessBaseSpringTestApplication             baseApplication;

    private Map<String, KouplelessBizSpringTestApplication> bizApplications = new HashMap<>();

    public KouplelessBizSpringTestApplication getBizApplication(String bizName) {
        return bizApplications.get(bizName);
    }

    public KouplelessTestMultiSpringApplication(KouplelessMultiSpringTestConfig config) {
        this.baseApplication = new KouplelessBaseSpringTestApplication(config.getBaseConfig());
        for (KouplelessBizSpringTestConfig bizConfig : config.getBizConfigs()) {
            this.bizApplications.put(bizConfig.getBizName(),
                new KouplelessBizSpringTestApplication(bizConfig));
        }
    }

    public void runBase() {
        baseApplication.run();
    }

    public void runBiz(String bizName) {
        bizApplications.get(bizName).initBiz(); // register biz to ark container
        bizApplications.get(bizName).run(); // run biz
    }

    public void run() {
        SOFAArkTestBootstrap.init(Thread.currentThread().getContextClassLoader());
        runBase();
        bizApplications.keySet().forEach(this::runBiz);
    }
}
