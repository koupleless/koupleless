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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
public class TestJunit5Class {
    @AfterAll
    public static void afterAll() {
        TestJunit5BootStrap.IN_BIZ_CLASSLOADER.set(false);
    }

    @Test
    public void TestIsInBizClassLoader() {
        System.out.println("IsInBizClassLoader " + TestJunit5BootStrap.IN_BIZ_CLASSLOADER.get());
        if (TestJunit5BootStrap.IN_BIZ_CLASSLOADER.get()) {
            Assertions.assertInstanceOf(BizClassLoader.class, Thread.currentThread()
                .getContextClassLoader());
        }
    }
}
