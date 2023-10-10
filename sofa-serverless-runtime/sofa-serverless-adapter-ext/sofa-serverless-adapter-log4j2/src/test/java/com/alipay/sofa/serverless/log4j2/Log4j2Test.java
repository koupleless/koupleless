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
package com.alipay.sofa.serverless.log4j2;

import com.alipay.sofa.serverless.log4j2.bootstrap.SOFABootApplication;
import com.alipay.sofa.serverless.log4j2.helper.Log1Print;
import com.alipay.sofa.serverless.log4j2.helper.Log2Print;
import com.alipay.sofa.serverless.log4j2.helper.LogHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

/**
 *
 * @author ruoshan
 * @version $Id: Log4j2Test.java, v 0.1 2018年10月16日 6:51 PM ruoshan Exp $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SOFABootApplication.class)
@ActiveProfiles("log4j2")
public class Log4j2Test {

    private String      log1Path;
    private String      log2Path;

    @Autowired
    private Environment environment;

    @Before
    public void before() {
        log1Path = System.getProperty("user.dir") + File.separator
                   + environment.getProperty(Log1Print.PATH_KEY);
        log2Path = System.getProperty("user.dir") + File.separator
                   + environment.getProperty(Log2Print.PATH_KEY);
    }

    @Test
    public void test() throws Exception {
        new Log1Print().printLog();
        new Log2Print().printLog();

        String appName = environment.getProperty("spring.application.name");
        String log1Content = LogHelper.readLogContent(log1Path, appName, "log1.log");
        System.out.println("log1Content: " + log1Content);
        Assert.assertTrue(log1Content.contains("log1"));
        Assert.assertTrue(log1Content.contains("INFO"));

        String log2Content = LogHelper.readLogContent(log2Path, appName, "log2.log");
        System.out.println("log2Content: " + log2Content);
        Assert.assertTrue(log2Content.contains("log2"));
        Assert.assertTrue(log2Content.contains("ERROR"));
    }

    @After
    public void after() {
        FileUtils.deleteQuietly(new File(log1Path));
        FileUtils.deleteQuietly(new File(log2Path));
    }

    @AfterClass
    public static void afterClass() {
        ((LoggerContext) LogManager.getContext(false)).close();
    }
}