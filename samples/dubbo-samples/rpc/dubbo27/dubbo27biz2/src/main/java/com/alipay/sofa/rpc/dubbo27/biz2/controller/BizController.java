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
package com.alipay.sofa.rpc.dubbo27.biz2.controller;

import javax.annotation.Resource;

import com.alipay.sofa.rpc.dubbo27.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo27.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo27.model.DemoService;
import com.alipay.sofa.rpc.dubbo27.model.HelloRequest;
import com.alipay.sofa.rpc.dubbo27.model.HelloResponse;
import com.alipay.sofa.rpc.dubbo27.model.HelloService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author syd
 * @version BizController.java, v 0.1 2023年10月31日 19:52 syd
 */
@RestController
public class BizController {

    @Resource
    private DemoService        demoServiceRef;

    @Resource
    private DemoService        secondDemoServiceRef;

    @Resource
    private HelloService       helloServiceRef;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public DemoResponse handle(String ref) {
        String appName = applicationContext.getId();
        DemoRequest demoRequest = new DemoRequest();
        demoRequest.setBiz(appName);
        if ("second".equals(ref)) {
            return secondDemoServiceRef.handle(demoRequest);
        }
        return demoServiceRef.handle(demoRequest);
    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    @ResponseBody
    public HelloResponse hello(String name) {
        String appName = applicationContext.getId();
        HelloRequest helloRequest = new HelloRequest();
        helloRequest.setName(name);
        return helloServiceRef.sayHello(helloRequest);
    }
}