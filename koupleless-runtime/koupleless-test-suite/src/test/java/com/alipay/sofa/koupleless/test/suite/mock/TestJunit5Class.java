package com.alipay.sofa.koupleless.test.suite.mock;

import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
public class TestJunit5Class {
    @AfterAll
    public static void afterAll() {
        TestJunit5BootStrap.IN_BIZ_CLASSLOADER.set(false);
    }

    @Test
    public void TestIsInBizClassLoader() {
        System.out.println("IsInBizClassLoader " + TestJunit5BootStrap.IN_BIZ_CLASSLOADER.get());
        if (TestJunit5BootStrap.IN_BIZ_CLASSLOADER.get()) {
            Assertions.assertInstanceOf(
                    BizClassLoader.class, Thread.currentThread().getContextClassLoader()
            );
        }
    }
}
