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
package com.alipay.sofa.koupleless.arklet.core.command.handler;

import java.util.List;

import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.handler.QueryAllBizHandler;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.koupleless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.exception.CommandValidationException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author mingmen
 * @date 2023/10/26
 */
public class QueryAllBizHandlerTest extends BaseHandlerTest {

    private QueryAllBizHandler handler;

    /**
     * 初始化测试对象
     */
    @Before
    public void setUp() {
        handler = (QueryAllBizHandler) commandService.getHandler(BuiltinCommand.QUERY_ALL_BIZ);
    }

    /**
     * 测试handle方法
     */
    @Test
    public void testHandle() {
        // 构造mock对象
        InputMeta inputMeta = mock(InputMeta.class);
        Output<List<BizInfo>> output = handler.handle(inputMeta);
        List<BizInfo> bizInfos = output.getData();
        assertEquals(5, bizInfos.size());
    }

    /**
     * 测试command方法
     */
    @Test
    public void testCommand() {
        assertEquals(BuiltinCommand.QUERY_ALL_BIZ, handler.command());
    }

    /**
     * 测试validate方法
     */
    @Test
    public void testValidate() {
        // mock对象
        InputMeta inputMeta = mock(InputMeta.class);

        // 调用validate方法
        try {
            handler.validate(inputMeta);
        } catch (CommandValidationException e) {
            // 不应该抛出异常
            assertEquals(null, e);
        }
    }
}
