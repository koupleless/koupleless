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
package com.alipay.sofa.serverless.common.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class MultiBizPropertiesTest {
    private final String key1 = "test-key-1";
    private final String value1 = "test-value-1";
    private final String value2 = "test-value-2";

    private ClassLoader baseClassLoader;


    @Before
    public void before() {
        Thread thread = Thread.currentThread();
        baseClassLoader = thread.getContextClassLoader();

        System.clearProperty(key1);
        MultiBizProperties.initSystem(URLClassLoader.class.getName());
    }


    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(baseClassLoader);
    }

    @Test
    public void testGetAndSet() {
        //base: set key1=value1, base get key1=value1
        Thread thread = Thread.currentThread();
        thread.setContextClassLoader(baseClassLoader);
        System.setProperty(key1, value1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader1 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: set key1=value2, biz1 get key1=value2
        System.setProperty(key1, value2);
        Assert.assertEquals(value2, System.getProperty(key1));
        //base: still get key1=value1
        thread.setContextClassLoader(baseClassLoader);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz2: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader2 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader2);
        Assert.assertEquals(value1, System.getProperty(key1));
    }

    @Test
    public void testGetAndClear() {
        //base: set key1=value1, base get key1=value1
        Thread thread = Thread.currentThread();
        thread.setContextClassLoader(baseClassLoader);
        System.setProperty(key1, value1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader1 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: set key1=value2, biz1 remove key1,so biz1 get key1 is null
        System.clearProperty(key1);
        Assert.assertNull(System.getProperty(key1));
        //base: still get key1=value1
        thread.setContextClassLoader(baseClassLoader);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz2: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader2 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader2);
        Assert.assertEquals(value1, System.getProperty(key1));
        //base: set key1=value2, base get key1=value2
        thread.setContextClassLoader(baseClassLoader);
        System.setProperty(key1, value2);
        Assert.assertEquals(value2, System.getProperty(key1));

        //biz1: the key1 is removed, biz1 get key1 is null
        thread.setContextClassLoader(loader1);
        Assert.assertNull(System.getProperty(key1));

        //biz2: not set key1 value, biz1 get key1=value1 as base
        thread.setContextClassLoader(loader2);
        Assert.assertEquals(value2, System.getProperty(key1));
    }

}