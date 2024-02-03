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

import com.alipay.sofa.rpc.dubbo3.model.CommonRequest;
import com.alipay.sofa.rpc.dubbo3.model.CommonResponse;
import com.alipay.sofa.rpc.dubbo3.model.CommonService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *  jvm服务，可以被基座调用
 * @author syd
 * @version TripleBizCommonService.java, v 0.1 2023年11月05日 16:02 syd
 */
@DubboService(group = "triplebiz")
public class TripleBizCommonService implements CommonService {
    @Override
    public CommonResponse sayHello(CommonRequest request) {
        CommonResponse response = new CommonResponse();
        response
            .setMessage(TripleBizCommonService.class.getName() + ": Hello," + request.getName());
        return response;
    }
}