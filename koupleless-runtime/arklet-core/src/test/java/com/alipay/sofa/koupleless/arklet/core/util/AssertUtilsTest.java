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
package com.alipay.sofa.koupleless.arklet.core.util;

import org.junit.Test;

public class AssertUtilsTest {

    @Test
    public void testTestAssertNotNull_Success() {
        Object instance = new Object();
        final String message = "instance is not null";
        AssertUtils.assertNotNull(instance, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestAssertNotNull_Failure() {
        final String message = "instance is  null";
        AssertUtils.assertNotNull(null, message);
    }

    @Test
    public void testTestAssertNull_Success() {
        final String message = "instance is null";
        AssertUtils.assertNull(null, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestAssertNull_Failure() {
        Object instance = new Object();
        final String message = "instance is not null";
        AssertUtils.assertNull(instance, message);
    }

    @Test
    public void testIsTrue_Success() {
        final boolean expression = true;
        final String message = "expression is true";
        AssertUtils.isTrue(expression, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsTrue_Failure() {
        final boolean expression = false;
        final String message = "expression is false";
        AssertUtils.isTrue(expression, message);
    }

    @Test
    public void testIsFalse_Success() {
        final boolean expression = false;
        final String message = "expression is false";
        AssertUtils.isFalse(expression, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsFalse_Failure() {
        final boolean expression = true;
        final String message = "expression is true";
        AssertUtils.isFalse(expression, message);
    }
}
