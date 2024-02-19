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
package com.alipay.sofa.rpc.dubbo26.base.controller;

import javax.annotation.Resource;

import com.alipay.sofa.rpc.dubbo26.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo26.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo26.model.DemoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author syd
 * @version BaseController.java, v 0.1 2023年12月02日 12:40 syd
 */
@RestController
public class BaseController {

    @Resource
    private DemoService        selfDemoServiceRef;

    @Resource
    private DemoService        bizInJvmDemoServiceRef;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 远程调用自己：base/com.alipay.sofa.rpc.dubbo26.model.DemoService
     * @return
     */
    @RequestMapping(value = "/selfRemote", method = RequestMethod.GET)
    @ResponseBody
    public DemoResponse selfRemote() {
        String appName = applicationContext.getId();
        DemoRequest helloRequest = new DemoRequest();
        helloRequest.setBiz(appName);
        return selfDemoServiceRef.handle(helloRequest);
    }

    /**
     * 调用模块：biz/com.alipay.sofa.rpc.dubbo26.model.DemoService
     * @return
     */
    @RequestMapping(value = "/bizInJvm", method = RequestMethod.GET)
    @ResponseBody
    public DemoResponse bizInJvm() {
        String appName = applicationContext.getId();
        DemoRequest helloRequest = new DemoRequest();
        helloRequest.setBiz(appName);
        return bizInJvmDemoServiceRef.handle(helloRequest);
    }
}