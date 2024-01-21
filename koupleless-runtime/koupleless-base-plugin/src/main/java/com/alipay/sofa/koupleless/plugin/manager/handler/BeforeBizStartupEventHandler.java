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
package com.alipay.sofa.koupleless.plugin.manager.handler;

import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;

/**
 * @author: yuanyuan
 * @date: 2023/9/26 5:47 下午
 */
public class BeforeBizStartupEventHandler implements EventHandler<BeforeBizStartupEvent> {

    @Override
    public void handleEvent(BeforeBizStartupEvent event) {
        Biz biz = event.getSource();
        BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(biz);
        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntimeContext);
    }

    @Override
    public int getPriority() {
        return PriorityOrdered.DEFAULT_PRECEDENCE;
    }
}
