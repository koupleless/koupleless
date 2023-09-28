package com.alipay.sofa.serverless.arklet.core.util;

import org.junit.Assert;
import org.junit.Test;

public class AssertUtilsTests {

    @Test
    public void testTestAssertNotNull_Success() {
        Object instance = new Object();
        final String message = "instance is not null";
        AssertUtils.assertNotNull(instance, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestAssertNotNull_Failure() {
        final String message = "instance is  null";
        AssertUtils.assertNotNull(null, message);
    }

    @Test
    public void testTestAssertNull_Success() {
        final String message = "instance is null";
        AssertUtils.assertNull(null, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestAssertNull_Failure() {
        Object instance = new Object();
        final String message = "instance is not null";
        AssertUtils.assertNull(instance, message);
    }

    @Test
    public void testIsTrue_Success() {
        final boolean expression = true;
        final String message = "expression is true";
        AssertUtils.isTrue(expression, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsTrue_Failure() {
        final boolean expression = false;
        final String message = "expression is false";
        AssertUtils.isTrue(expression, message);
    }

    @Test
    public void testIsFalse_Success() {
        final boolean expression = false;
        final String message = "expression is false";
        AssertUtils.isFalse(expression, message);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsFalse_Failure() {
        final boolean expression = true;
        final String message = "expression is true";
        AssertUtils.isFalse(expression, message);
    }
}