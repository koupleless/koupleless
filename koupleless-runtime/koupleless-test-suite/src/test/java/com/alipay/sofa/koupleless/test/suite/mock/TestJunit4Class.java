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
package com.alipay.sofa.koupleless.test.suite.mock;

import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
public class TestJunit4Class {
    @AfterClass
    public static void afterAll() {
        TestJunit4BootStrap.IN_BIZ_CLASSLOADER.set(false);
    }

    @Test
    public void testWhereAmI() {
        System.out.println("IsInBizClassLoader " + TestJunit4BootStrap.IN_BIZ_CLASSLOADER.get());
        if (TestJunit4BootStrap.IN_BIZ_CLASSLOADER.get()) {
            Assert
                .assertTrue(Thread.currentThread().getContextClassLoader() instanceof BizClassLoader);
        }
    }
}
