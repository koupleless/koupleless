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
package com.alipay.sofa.rpc.dubbo3.triplebiz.service;

import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoResponse;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *
 * @author syd
 * @version FastJson2DemoService.java, v 0.1 2023年11月08日 12:52 syd
 */
@DubboService(group = "fastjson2")
public class FastJson2DemoService implements DemoService {
    @Override
    public DemoResponse sayHello(DemoRequest request) {
        DemoResponse response = new DemoResponse();
        response.setMessage(FastJson2DemoService.class.getName() + ": Hello," + request.getName());
        return response;
    }
}