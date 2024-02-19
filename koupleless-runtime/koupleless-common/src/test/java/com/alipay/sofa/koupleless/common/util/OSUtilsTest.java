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
package com.alipay.sofa.koupleless.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author CodeNoobKing
 * @data 2024/1/22
 */
public class OSUtilsTest {

    @Test
    public void testGetLocalFileProtocolPrefix() {
        try {
            OSUtils.OS_NAME_KEY = "mock.os.name";
            System.setProperty("mock.os.name", "Windows 7");
            Assert.assertEquals("file:///", OSUtils.getLocalFileProtocolPrefix());

            System.setProperty("mock.os.name", "Linux");
            Assert.assertEquals("file://", OSUtils.getLocalFileProtocolPrefix());

            System.setProperty("mock.os.name", "Mac OS X");
            Assert.assertEquals("file://", OSUtils.getLocalFileProtocolPrefix());
        } finally {
            OSUtils.OS_NAME_KEY = "os.name";
        }

    }
}
