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
package com.alipay.sofa.rpc.dubbo26.biz.controller;

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
 * @version BizController.java, v 0.1 2023年10月31日 19:52 syd
 */
@RestController
public class BizController {

    @Resource
    private DemoService        selfDemoServiceRef;

    @Resource
    private DemoService        baseDemoServiceRef;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 远程调用自己：biz1/com.alipay.sofa.rpc.dubbo26.model.DemoService
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
     * 本地jvm调用基座，通信的模型，需要模块provided引入，基座compile引入
     * @return
     */
    @RequestMapping(value = "/baseInJvm", method = RequestMethod.GET)
    @ResponseBody
    public DemoResponse baseInJvm() {
        String appName = applicationContext.getId();
        DemoRequest helloRequest = new DemoRequest();
        helloRequest.setBiz(appName);
        return baseDemoServiceRef.handle(helloRequest);
    }
}