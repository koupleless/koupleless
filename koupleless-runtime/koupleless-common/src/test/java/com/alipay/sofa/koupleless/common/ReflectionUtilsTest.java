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
package com.alipay.sofa.koupleless.common;

import com.alipay.sofa.koupleless.common.util.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionUtilsTest {
    @Test
    public void testIsValid() {
        StackTraceElement nativeMethodElement = new StackTraceElement("ClassName", "methodName",
            "fileName", -2);
        Assert.assertFalse(ReflectionUtils.isValid(nativeMethodElement));

        StackTraceElement sunReflectElement = new StackTraceElement("sun.reflect.ClassName",
            "methodName", "fileName", 123);
        Assert.assertFalse(ReflectionUtils.isValid(sunReflectElement));

        StackTraceElement javaReflectInvokeElement = new StackTraceElement(
            "java.lang.reflect.ClassName", "invoke", "fileName", 123);
        Assert.assertFalse(ReflectionUtils.isValid(javaReflectInvokeElement));

        StackTraceElement javaReflectNewInstanceElement = new StackTraceElement(
            "java.lang.reflect.ClassName", "newInstance", "fileName", 123);
        Assert.assertFalse(ReflectionUtils.isValid(javaReflectNewInstanceElement));

        StackTraceElement jdkInternalReflectElement = new StackTraceElement(
            "jdk.internal.reflect.ClassName", "methodName", "fileName", 123);
        Assert.assertFalse(ReflectionUtils.isValid(jdkInternalReflectElement));

        StackTraceElement javaLangClassElement = new StackTraceElement("java.lang.Class",
            "newInstance", "fileName", 123);
        Assert.assertFalse(ReflectionUtils.isValid(javaLangClassElement));

        StackTraceElement javaLangInvokeMethodHandleElement = new StackTraceElement(
            "java.lang.invoke.MethodHandle", "invokeMethod", "fileName", 123);
        Assert.assertFalse(ReflectionUtils.isValid(javaLangInvokeMethodHandleElement));

        StackTraceElement validElement = new StackTraceElement("ValidClassName", "validMethodName",
            "fileName", 123);
        Assert.assertTrue(ReflectionUtils.isValid(validElement));
    }
}
