package com.alipay.sofa.koupleless.test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
public class TestJunit5BootStrap {
    public static AtomicBoolean IN_BIZ_CLASSLOADER = new AtomicBoolean(false);

    public void bootstrapBase() {
        System.out.println("TestJunit5BootStrap");
        IN_BIZ_CLASSLOADER.set(true);
    }
}
