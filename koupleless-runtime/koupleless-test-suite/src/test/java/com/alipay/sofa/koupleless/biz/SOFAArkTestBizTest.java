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
package com.alipay.sofa.koupleless.biz;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
public class SOFAArkTestBizTest {

    private static SOFAArkTestBiz testBiz;

    @BeforeAll
    public static void setUp() {
        SOFAArkServiceContainerSingleton.init(SOFAArkTestBizTest.class.getClassLoader());

        List<String> testClassNames = new ArrayList<>();
        testClassNames.add("com.alipay.sofa.koupleless.test.LoadByTestBizClassA");
        testClassNames.add("com.alipay.sofa.koupleless.test.LoadByTestBizClassB");
        List<String> includeClassPatterns = new ArrayList<>();

        testBiz = new SOFAArkTestBiz("com.alipay.sofa.koupleless.test.BootStrapClass", "test",
            "1.0.0", testClassNames, includeClassPatterns,
            (URLClassLoader) SOFAArkTestBizTest.class.getClassLoader());
    }

    @Test
    public void testTestBiz() {

        {
            Assertions.assertTrue(testBiz.isDeclaredMode());
            Assertions.assertTrue(testBiz.isDeclared(null, null));
        }

        Assertions.assertEquals(2, testBiz.getTestClasses().size());

        testBiz.executeTest(() -> {
            Assertions.assertInstanceOf(SOFAArkTestBizClassLoader.class, Thread.currentThread().getContextClassLoader());
        });
    }
}
