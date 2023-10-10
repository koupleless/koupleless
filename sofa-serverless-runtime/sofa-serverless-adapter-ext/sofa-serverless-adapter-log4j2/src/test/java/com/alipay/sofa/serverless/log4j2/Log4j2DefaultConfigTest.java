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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

/**
 *
 * @author ruoshan
 * @version $Id: Log4j2DefaultConfigTest.java, v 0.1 2019年04月25日 12:24 ruoshan Exp $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SOFABootApplication.class)
public class Log4j2DefaultConfigTest {

    private String       log1Path;
    private String       log2Path;

    @Autowired
    private Environment  environment;

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Before
    public void before() {
        log1Path = System.getProperty("user.dir") + File.separator
                   + environment.getProperty(Log1Print.PATH_KEY);
        log2Path = System.getProperty("user.dir") + File.separator
                   + environment.getProperty(Log2Print.PATH_KEY);
    }

    @Test
    public void testDefaultConfig() {
        new Log1Print().printLog();
        new Log2Print().printLog();

        String systemLog = systemOutRule.getLog();
        Assert.assertTrue(systemLog.contains("log1"));
        Assert.assertTrue(systemLog.contains("log2"));
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