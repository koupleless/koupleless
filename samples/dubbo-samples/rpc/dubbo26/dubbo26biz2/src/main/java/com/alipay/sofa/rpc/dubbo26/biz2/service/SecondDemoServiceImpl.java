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
package com.alipay.sofa.rpc.dubbo26.biz2.service;

import com.alipay.sofa.rpc.dubbo26.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo26.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo26.model.DemoService;

/**
 *
 * @author syd
 * @version SecondDemoServiceImpl.java, v 0.1 2023年11月02日 15:04 syd
 */
public class SecondDemoServiceImpl implements DemoService {
    @Override
    public DemoResponse handle(DemoRequest demoRequest) {
        DemoResponse demoResponse = new DemoResponse();
        demoResponse.setResult(demoRequest.getBiz() + "->" + getClass().getName());
        return demoResponse;
    }
}