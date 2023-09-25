package com.alipay.sofa.serverless.arklet.core.util;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class ConvertUtilsTest extends TestCase {

    @Test
    public void testBytes2Megabyte() {
        final long bytes = 1024 * 1024;
        final double delta = 1e-5;
        Assert.assertEquals(1., ConvertUtils.bytes2Megabyte(bytes), delta);
    }

    @Test
    public void testGetDurationSecond() {
        try {
            final Date date = new Date(System.currentTimeMillis());
            final long millis = 1000;
            final double delta = 1e-2;
            Thread.sleep(millis);
            Assert.assertEquals(millis / 1000., ConvertUtils.getDurationSecond(date), delta);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}