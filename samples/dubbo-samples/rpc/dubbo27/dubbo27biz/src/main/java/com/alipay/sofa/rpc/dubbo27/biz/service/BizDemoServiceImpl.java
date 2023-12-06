/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo27.biz.service;

import com.alipay.sofa.rpc.dubbo27.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo27.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo27.model.DemoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author syd
 * @version BizDemoServiceImpl.java, v 0.1 2023年10月31日 19:48 syd
 */
public class BizDemoServiceImpl implements DemoService {
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public DemoResponse handle(DemoRequest demoRequest) {
        DemoResponse response = new DemoResponse();
        response.setResult(demoRequest.getBiz() + "->" + getClass().getName());
        return response;
    }
}