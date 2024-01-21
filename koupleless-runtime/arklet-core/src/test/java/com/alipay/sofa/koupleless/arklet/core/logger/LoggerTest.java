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
package com.alipay.sofa.koupleless.arklet.core.logger;

import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import static org.mockito.Mockito.*;

/**
 * @author mingmen
 * @date 2023/10/26
 */

public class LoggerTest {

    @Mock
    private Logger       logger;
    private ArkletLogger arkletLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        arkletLogger = new ArkletLogger(logger);
    }

    /**
     * 测试用例设计思路：
     * 测试用例编号：001
     * 测试方法功能：验证当日志级别为trace时，是否正确打印日志消息
     * 测试数据：msg为"test message"
     * 预期结果：该日志消息应该被打印出来
     */
    @Test
    public void testTrace() {
        String msg = "test message";

        arkletLogger.trace(msg);

        verify(logger).trace(msg);
    }

    /**
     * 测试用例设计思路：
     * 测试用例编号：002
     * 测试方法功能：验证当日志级别为trace时，是否正确打印带参数的日志消息
     * 测试数据：format为"test message, arg={}", arg为"arg1"
     * 预期结果：该带参数的日志消息应该被打印出来，且参数arg的值为"arg1"
     */
    @Test
    public void testTraceWithArg() {
        String format = "test message, arg={}";
        String arg = "arg1";

        arkletLogger.trace(format, arg);

        verify(logger).trace(format, arg);
    }

    /**
     * 测试用例设计思路：
     * 测试用例编号：003
     * 测试方法功能：验证当日志级别为trace时，是否正确打印带多个参数的日志消息
     * 测试数据：format为"test message, arg1={}, arg2={}", arg1为"arg1", arg2为"arg2"
     * 预期结果：该带多个参数的日志消息应该被打印出来，且参数arg1的值为"arg1"，参数arg2的值为"arg2"
     */
    @Test
    public void testTraceWithMultipleArgs() {
        String format = "test message, arg1={}, arg2={}";
        String arg1 = "arg1";
        String arg2 = "arg2";

        arkletLogger.trace(format, arg1, arg2);

        verify(logger).trace(format, arg1, arg2);
    }

    /**
     * 测试用例设计思路：
     * 测试用例编号：004
     * 测试方法功能：验证当日志级别为trace时，是否正确打印带多个参数的日志消息
     * 测试数据：format为"test message, arg1={}, arg2={}, arg3={}", arguments为["arg1", "arg2", "arg3"]
     * 预期结果：该带多个参数的日志消息应该被打印出来，且参数arg1的值为"arg1"，参数arg2的值为"arg2"，参数arg3的值为"arg3"
     */
    @Test
    public void testTraceWithArgumentArray() {
        String format = "test message, arg1={}, arg2={}, arg3={}";
        String[] arguments = { "arg1", "arg2", "arg3" };

        arkletLogger.trace(format, (Object[]) arguments);

        verify(logger).trace(format, (Object[]) arguments);
    }

    /**
     * 测试用例设计思路：
     * 测试用例编号：005
     * 测试方法功能：验证当日志级别为trace时，是否正确打印带异常信息的日志消息
     * 测试数据：msg为"test message", t为Exception对象
     * 预期结果：该带异常信息的日志消息应该被打印出来，且异常信息为Exception对象
     */
    @Test
    public void testTraceWithThrowable() {
        String msg = "test message";
        Throwable t = new Exception();

        arkletLogger.trace(msg, t);

        verify(logger).trace(msg, t);
    }
}
