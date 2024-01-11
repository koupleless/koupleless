/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo27.biz.controller;

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
    private DemoService demoServiceRef;

    @Resource
    private DemoService secondDemoServiceRef;

    @Resource
    private HelloService helloServiceRef;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public DemoResponse handle(String ref) {
        String appName = applicationContext.getId();
        DemoRequest demoRequest = new DemoRequest();
        demoRequest.setBiz(appName);
        if("second".equals(ref)){
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

    public void setDemoServiceRef(DemoService demoServiceRef) {
        this.demoServiceRef = demoServiceRef;
    }

    public void setSecondDemoServiceRef(DemoService secondDemoServiceRef) {
        this.secondDemoServiceRef = secondDemoServiceRef;
    }

    public void setHelloServiceRef(HelloService helloServiceRef) {
        this.helloServiceRef = helloServiceRef;
    }
}