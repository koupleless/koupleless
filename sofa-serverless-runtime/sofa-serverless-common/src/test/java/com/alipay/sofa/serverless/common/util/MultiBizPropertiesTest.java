package com.alipay.sofa.serverless.common.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class MultiBizPropertiesTest {
    private final String key1 = "test-key-1";
    private final String value1 = "test-value-1";
    private final String value2 = "test-value-2";

    @Before
    public void before() {
        System.clearProperty(key1);
        MultiBizProperties.initSystem(URLClassLoader.class.getName());
    }

    @Test
    public void testGetAndSet() {
        //base: set key1=value1, base get key1=value1
        Thread thread = Thread.currentThread();
        thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
        System.setProperty(key1, value1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader1 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: set key1=value2, biz1 get key1=value2
        System.setProperty(key1, value2);
        Assert.assertEquals(value2, System.getProperty(key1));
        //base: still get key1=value1
        thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz2: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader2 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader2);
        Assert.assertEquals(value1, System.getProperty(key1));
    }


    @Test
    public void testGetAndClear() {
        //base: set key1=value1, base get key1=value1
        Thread thread = Thread.currentThread();
        thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
        System.setProperty(key1, value1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader1 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader1);
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz1: set key1=value2, biz1 get key1=value2
        System.clearProperty(key1);
        Assert.assertNull(System.getProperty(key1));
        //base: still get key1=value1
        thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
        Assert.assertEquals(value1, System.getProperty(key1));
        //biz2: not set key1 value, biz1 get key1=value1 as base
        ClassLoader loader2 = new URLClassLoader(new URL[0]);
        thread.setContextClassLoader(loader2);
        Assert.assertEquals(value1, System.getProperty(key1));
        //base: set key1=value2, base get key1=value2
        thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
        System.setProperty(key1, value2);
        Assert.assertEquals(value2, System.getProperty(key1));

        //biz1: the key1 is removed, biz1 get key1 is null
        thread.setContextClassLoader(loader1);
        Assert.assertNull(System.getProperty(key1));

        //biz2: not set key1 value, biz1 get key1=value1 as base
        thread.setContextClassLoader(loader2);
        Assert.assertEquals(value2, System.getProperty(key1));
    }


}