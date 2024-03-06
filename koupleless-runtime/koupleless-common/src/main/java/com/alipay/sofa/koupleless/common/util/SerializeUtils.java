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

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 * @author: yuanyuan
 * @date: 2023/9/25 8:22 下午
 */
public class SerializeUtils {

    private static Object _serializeTransform(Object source, ClassLoader targetClassLoader) {
        try {
            if (source == null) {
                return source;
            }
            Class<?> sourceClass;
            if (source.getClass().equals(Class.class)) {
                sourceClass = (Class<?>) source;
            } else {
                sourceClass = source.getClass();
            }
            if (sourceClass.getClassLoader() == targetClassLoader
                || targetClassLoader.loadClass(sourceClass.getName()).getClassLoader() == sourceClass
                    .getClassLoader()) {
                return source;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Object target;
        ClassLoader currentContextClassloader = Thread.currentThread().getContextClassLoader();
        try {
            if (targetClassLoader != null) {
                Thread.currentThread().setContextClassLoader(targetClassLoader);
            }

            // 支持多态的序列化与反序列化，需要使用 hessian
            SerializerFactory serializerFactory = new SerializerFactory();
            serializerFactory.setAllowNonSerializable(true);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Hessian2Output h2o = new Hessian2Output(bos);
            h2o.setSerializerFactory(serializerFactory);
            h2o.writeObject(source);
            h2o.flush();
            byte[] content = bos.toByteArray();

            Hessian2Input h2i = new Hessian2Input(new ByteArrayInputStream(content));
            h2i.setSerializerFactory(serializerFactory);
            target = h2i.readObject();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(currentContextClassloader);
        }
        return target;
    }

    public static Object serializeTransform(Object originalSource, ClassLoader targetClassLoader) {
        if (originalSource == null) {
            return null;
        }
        if (originalSource.getClass().isArray()) {
            Object[] sources = (Object[]) originalSource;
            if (sources.length > 0) {
                Object[] targets = (Object[]) Array.newInstance(sources.getClass()
                    .getComponentType(), sources.length);

                for (int i = 0; i < sources.length; i++) {
                    targets[i] = _serializeTransform(sources[i], targetClassLoader);
                }
                return (Object) targets;
            } else {
                return originalSource;
            }
        } else {
            return _serializeTransform(originalSource, targetClassLoader);
        }
    }
}
