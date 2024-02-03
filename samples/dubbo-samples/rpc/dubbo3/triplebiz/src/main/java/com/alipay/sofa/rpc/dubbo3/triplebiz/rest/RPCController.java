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
package com.alipay.sofa.rpc.dubbo3.triplebiz.rest;

import com.alipay.sofa.rpc.dubbo3.model.CommonRequest;
import com.alipay.sofa.rpc.dubbo3.model.CommonResponse;
import com.alipay.sofa.rpc.dubbo3.model.CommonService;
import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo3.triplebiz.service.DemoService;
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

    /**
     * tri协议，远程调用，默认走hessian序列化
     */
    @DubboReference(group = "triplebiz", scope = "remote")
    private DemoService        demoService;

    /**
     * tri协议，injvm调用，scope默认走injvm
     */
    @DubboReference(group = "base")
    private CommonService      commonServiceInJvm;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/remote", method = RequestMethod.GET)
    public String remote() {
        try {
            String appName = applicationContext.getId();
            DemoRequest demoRequest = new DemoRequest();
            demoRequest.setName(appName);
            DemoResponse response = demoService.sayHello(demoRequest);
            log.info(response.getMessage());
            return response.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/injvm", method = RequestMethod.GET)
    public String injvm() {
        try {
            String appName = applicationContext.getId();
            CommonRequest demoRequest = new CommonRequest();
            demoRequest.setName(appName);
            CommonResponse response = commonServiceInJvm.sayHello(demoRequest);
            log.info(response.getMessage());
            return response.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
