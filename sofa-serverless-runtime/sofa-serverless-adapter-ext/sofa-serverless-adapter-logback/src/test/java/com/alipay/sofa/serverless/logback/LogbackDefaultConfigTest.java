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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author : chenlei3641
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SOFABootApplication.class)
public class LogbackDefaultConfigTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void testDefaultConfig() {
        new Log1Print().printLog();
        new Log2Print().printLog();

        String systemLog = systemOutRule.getLog();
        Assert.assertTrue(systemLog.contains("log1"));
        Assert.assertTrue(systemLog.contains("log2"));
    }
}