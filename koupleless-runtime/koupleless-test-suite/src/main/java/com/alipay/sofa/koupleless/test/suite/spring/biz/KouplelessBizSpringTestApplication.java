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
package com.alipay.sofa.koupleless.test.suite.spring.biz;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBiz;
import com.alipay.sofa.koupleless.test.suite.spring.framwork.KouplelessSpringTestUtils;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBizSpringTestConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author CodeNoobKing
 * @date 2024/3/6
 */
@Getter
public class KouplelessBizSpringTestApplication {

    private SOFAArkTestBiz                 testBiz;

    private ConfigurableApplicationContext applicationContext;

    private KouplelessBizSpringTestConfig  config;

    @SneakyThrows
    public KouplelessBizSpringTestApplication(KouplelessBizSpringTestConfig config) {
        config.init();
        this.config = config;
    }

    public boolean isExcludedDependency(String dependency) {
        for (String regexp : CollectionUtils.emptyIfNull(KouplelessSpringTestUtils.getConfig()
            .getBiz().getExcludeDependencyRegexps())) {
            if (dependency.matches(".*" + regexp + ".*")) {
                return true;
            }
        }

        for (String excludePackage : CollectionUtils.emptyIfNull(config.getExcludePackages())) {
            if (dependency.matches(".*" + excludePackage + ".*")) {
                return true;
            }
        }
        return false;
    }

    public void initBiz() {
        ArrayList<String> includeClassPatterns = new ArrayList<>();
        includeClassPatterns.add(config.getPackageName() + ".*");
        URLClassLoader tccl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        List<URL> excludedUrls = new ArrayList<>();
        for (URL url : tccl.getURLs()) {
            if (!isExcludedDependency(url.toString())) {
                excludedUrls.add(url);
            }
        }

        testBiz = new SOFAArkTestBiz("", config.getBizName(), "TEST", new ArrayList<>(),
            includeClassPatterns, new URLClassLoader(excludedUrls.toArray(new URL[0]),
                tccl.getParent()));
        testBiz.setWebContextPath(config.getBizName());
    }

    @SneakyThrows
    public void run() {
        CompletableFuture.runAsync(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.currentThread().setContextClassLoader(testBiz.getBizClassLoader());
                EventAdminService eventAdminService = ArkClient.getEventAdminService();
                eventAdminService.sendEvent(new BeforeBizStartupEvent(testBiz));
                Class<?> mainClass = testBiz.getBizClassLoader().loadClass(config.getMainClass());
                SpringApplication springApplication = new SpringApplication(mainClass);
                springApplication.setAdditionalProfiles(config.getBizName());
                applicationContext = springApplication.run();
                eventAdminService.sendEvent(new AfterBizStartupEvent(testBiz));

            }
        }, new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        }).get();
    }

}
