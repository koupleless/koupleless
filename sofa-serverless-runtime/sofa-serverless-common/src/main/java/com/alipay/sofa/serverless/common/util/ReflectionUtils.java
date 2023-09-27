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

import java.lang.reflect.Method;

/**
 * @author: yuanyuan
 * @date: 2023/9/25 5:06 下午
 */
public class ReflectionUtils {

    private static Method method;

    static {
        try {
            Class<?> clazz = Class.forName("sun.reflect.Reflection");
            method = clazz.getDeclaredMethod("getCallerClass", new Class[] { int.class });
            method.setAccessible(true);
        } catch (Exception e) {
            method = null;
        }
    }

    public static Class<?> getCallerClass(int realFramesToSkip) {
        if (method == null)
            throw new IllegalStateException("sun.reflect.Reflection initialization failure.");
        try {
            return (Class<?>) method.invoke(null, realFramesToSkip);
        } catch (Exception e) {
            throw new IllegalStateException(
                "An error occurs when invoking the sun.reflect.Reflection#getCallerClass", e);
        }
    }

}
