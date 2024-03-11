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
package com.alipay.sofa.koupleless.test.suite.biz;

import com.alipay.sofa.koupleless.test.suite.mock.LoadByBaseClass;
import com.alipay.sofa.koupleless.test.suite.mock.LoadByTestBizClassA;
import org.junit.Assert;
import org.junit.Test;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
public class SOFAArkTestBizClassLoaderTest {

    private URLClassLoader baseClassLoader = new URLClassLoader(((URLClassLoader) Thread
                                               .currentThread().getContextClassLoader()).getURLs(),
                                               Thread.currentThread().getContextClassLoader());

    @Test
    public void testResolveLocalClass() throws Throwable {
        SOFAArkTestBootstrap.init(baseClassLoader);

        String bizIdentity = "bizIdentity";
        List<String> includeClassNames = new ArrayList<>();
        includeClassNames.add(LoadByTestBizClassA.class.getName());
        List<Pattern> includeClassPatterns = new ArrayList<>();
        includeClassPatterns.add(Pattern.compile(".*mock\\.LoadByTestBizClassB.*"));

        SOFAArkTestBizClassLoader testBizClassLoader = new SOFAArkTestBizClassLoader(bizIdentity,
            includeClassNames, includeClassPatterns, baseClassLoader);
        Assert.assertNull(testBizClassLoader.resolveLocalClass(LoadByBaseClass.class.getName()));

        Assert.assertEquals(testBizClassLoader,
            testBizClassLoader.resolveLocalClass(LoadByTestBizClassA.class.getName())
                .getClassLoader());

        Assert.assertEquals(
            testBizClassLoader,
            testBizClassLoader.resolveLocalClass(
                "com.alipay.sofa.koupleless.test.suite.mock.LoadByTestBizClassB").getClassLoader());

    }
}
