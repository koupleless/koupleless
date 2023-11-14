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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * @author: yuanyuan
 * @date: 2023/9/25 5:06 下午
 */
public class ReflectionUtils {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static Method                     method;

    static {
        try {
            Class<?> clazz = Class.forName("sun.reflect.Reflection");
            method = clazz.getDeclaredMethod("getCallerClass", new Class[] { int.class });
            method.setAccessible(true);
        } catch (Exception e) {
            method = null;
        }
    }

    public static Class<?> executeJDK8Logic(int realFramesToSkip) {
        // 在 JDK 8 下执行的方法逻辑
        if (method == null)
            throw new IllegalStateException("sun.reflect.Reflection initialization failure.");
        try {
            return (Class<?>) method.invoke(null, realFramesToSkip);
        } catch (Exception e) {
            throw new IllegalStateException(
                "An error occurs when invoking the sun.reflect.Reflection#getCallerClass", e);
        }
    }

    public static Class<?> executeJDK17Logic(int depth) {
        // 在 JDK 17 下执行的方法逻辑
        try {
            java.lang.StackWalker walker = java.lang.StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            return walker.walk(frames -> frames.skip(depth + 1).findFirst().map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
        } catch (Exception e) {
            throw new IllegalStateException("sun.reflect.Reflection initialization failure.");
        }
    }

    public static Class<?> getCallerClass(int realFramesToSkip) {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.8")) {
            // JDK 8 版本的逻辑
            // 执行 JDK 8 版本下的方法
            return executeJDK8Logic(realFramesToSkip);
        } else if (javaVersion.startsWith("17")) {
            // JDK 17 版本的逻辑
            // 执行 JDK 17 版本下的方法
            return executeJDK17Logic(realFramesToSkip);
        }
        return null;
    }

}
