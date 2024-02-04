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
package com.alipay.sofa.rpc.dubbo3.grpcbiz.rest;

import org.apache.dubbo.common.constants.CommonConstants;
import com.alipay.sofa.rpc.dubbo3.model.Greeter;
import com.alipay.sofa.rpc.dubbo3.model.GreeterReply;
import com.alipay.sofa.rpc.dubbo3.model.GreeterRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RPCController {

    @DubboReference(group = "grpcbiz", scope = "remote", proxy = CommonConstants.NATIVE_STUB)
    private Greeter            greeter;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getId();

        GreeterRequest greeterRequest = GreeterRequest.newBuilder().setName(appName).build();
        GreeterReply response = greeter.greet(greeterRequest);
        log.info(response.getMessage());
        return response.getMessage();
    }
}
