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
package com.alipay.sofa.koupleless.spring.loader;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.junit.Assert;

public class CachedLaunchedURLClassLoaderTest extends TestCase {

    public void testLoadClass() throws IOException, ClassNotFoundException, InterruptedException {

        URL appJar = getClass().getClassLoader().getResource("jars/demo.jar");

        System.setProperty("serverless.class.cache.size", "100");
        CachedLaunchedURLClassLoader loader = new CachedLaunchedURLClassLoader(false, null,
                new URL[] { appJar }, null);
        URL url = loader.getResource("com/example/demo/DemoApplication.class");
        assertNotNull(url);
        assertEquals(loader.getResource("com/example/demo/DemoApplication.class"), url);
        assertEquals(loader.loadClass("com.example.demo.DemoApplication"),
                loader.loadClass("com.example.demo.DemoApplication"));
        assertNull(loader.getResource("demo/ApplicationNotExist.class"));
        assertTrue(!loader.getResources("demo/ApplicationNotExist.class").hasMoreElements());
        assertTrue(!loader.getResources("demo/ApplicationNotExist.class").hasMoreElements());

        ClassNotFoundException ex = null;
        ClassNotFoundException ex1 = null;
        ClassNotFoundException ex2 = null;
        ClassNotFoundException ex3 = null;
        try {
            loader.loadClass("demo.ApplicationNotExist");
        } catch (ClassNotFoundException exception) {
            ex = exception;
        }
        try {
            loader.loadClass("demo.ApplicationNotExist");
        } catch (ClassNotFoundException exception) {
            ex1 = exception;
        }
        try {
            loader.loadClass("demo.ApplicationNotExist");
        } catch (ClassNotFoundException exception) {
            ex2 = exception;
        }
        try {
            loader.clearCache();
            loader.loadClass("demo.ApplicationNotExist");
        } catch (ClassNotFoundException exception) {
            ex3 = exception;
        }
        assertNotNull(ex);
        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);

        assertEquals(ex.getMessage(), ex1.getMessage());
        assertNotSame(ex, ex2);
        assertNotSame(ex1, ex2);
        assertNotSame(ex, ex3);
        assertNotSame(ex1, ex3);
        assertNotSame(ex2, ex3);
        ExecutorService executor = new ThreadPoolExecutor(5, 10, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(100));
        CountDownLatch countDownLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            final String className = "notExits" + i;
            executor.submit(() -> {
                try {
                    loader.loadClass(className);
                } catch (ClassNotFoundException e) {
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        Assert.assertEquals(99, loader.classCache.size());
    }
}
