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

/**
 * <em>Consider this class private.</em> Utility class for ClassLoaders.
 *
 * @see ClassLoader
 * @see RuntimePermission
 * @see Thread#getContextClassLoader()
 * @see ClassLoader#getSystemClassLoader()
 */
public final class LoaderUtil {
    private LoaderUtil() {
    }

    /**
     * Gets the current Thread ClassLoader. Returns the system ClassLoader if the TCCL is {@code null}. If the system
     * ClassLoader is {@code null} as well, then the ClassLoader for this class is returned. If running with a
     * {@link SecurityManager} that does not allow access to the Thread ClassLoader or system ClassLoader, then the
     * ClassLoader for this class is returned.
     *
     * @return the current ThreadContextClassLoader.
     */
    public static ClassLoader getThreadContextClassLoader() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            return cl;
        }
        final ClassLoader ccl = LoaderUtil.class.getClassLoader();
        return ccl == null ? ClassLoader.getSystemClassLoader() : ccl;
    }

    /**
     * @param className The class name.
     * @return the Class for the given name.
     * @throws ClassNotFoundException if the specified class name could not be found
     * @since 2.1
     */
    public static Class<?> loadClass(final String className) throws ClassNotFoundException {
        try {
            return getThreadContextClassLoader().loadClass(className);
        } catch (final Throwable ignored) {
            return Class.forName(className);
        }
    }

}
