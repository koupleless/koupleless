/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
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
    private DemoService selfDemoServiceRef;

    @Resource
    private DemoService bizInJvmDemoServiceRef;

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