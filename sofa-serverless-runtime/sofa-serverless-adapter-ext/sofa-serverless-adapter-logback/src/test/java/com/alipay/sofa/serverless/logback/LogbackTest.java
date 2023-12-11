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
package com.alipay.sofa.serverless.logback;

import com.alipay.sofa.serverless.logback.bootstrap.SOFABootApplication;
import com.alipay.sofa.serverless.logback.helper.Log1Print;
import com.alipay.sofa.serverless.logback.helper.Log2Print;
import com.alipay.sofa.serverless.logback.helper.LogHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author : chenlei3641
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SOFABootApplication.class)
@ActiveProfiles("logback")
public class LogbackTest {
    @Autowired
    private Environment environment;

    @Test
    public void test() throws Exception {
        new Log1Print().printLog();
        new Log2Print().printLog();
        String appName = environment.getProperty("spring.application.name");

        String log1Content = LogHelper.readLogContent("log", appName + ".log");
        System.out.println("log1Content: " + log1Content);
        Assert.assertTrue(log1Content.contains("log1"));
        Assert.assertTrue(log1Content.contains("INFO"));

        String log2Content = LogHelper.readLogContent("log", appName + ".log");
        System.out.println("log2Content: " + log2Content);
        Assert.assertTrue(log2Content.contains("log2"));
        Assert.assertTrue(log2Content.contains("ERROR"));
    }
}
