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
package com.alipay.sofa.koupleless.support.dubbo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author: yuanyuan
 * @date: 2023/12/25 4:24 下午
 */
public class ReflectionUtils {

    public static <T> T getField(Object target, Class<?> clazz, String fieldName) {
        try {
            Field filed = clazz.getDeclaredField(fieldName);
            filed.setAccessible(true);
            Object value = filed.get(target);
            return value == null ? null : (T) value;
        } catch (Exception e) {
            throw new RuntimeException(
                "get field " + clazz.getName() + "@" + fieldName + " failed", e);
        }
    }

    public static <T> void setField(Object target, Class<?> clazz, String fieldName, T value) {
        try {
            Field filed = clazz.getDeclaredField(fieldName);
            filed.setAccessible(true);
            filed.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(
                "set filed " + clazz.getName() + "@" + fieldName + " failed", e);
        }
    }

    public static <T> T getStaticField(Class<?> clazz, String fieldName) {
        return getField(null, clazz, fieldName);
    }

    public static <T> void setStaticField(Class<?> clazz, String fieldName, T value) {
        setField(null, clazz, fieldName, value);
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("get method " + clazz.getName() + "@" + name + " failed", e);
        }
    }

}
