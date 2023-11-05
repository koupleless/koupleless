/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo26.biz.service;

import com.alipay.sofa.rpc.dubbo26.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo26.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo26.model.DemoService;

/**
 *
 * @author syd
 * @version SecondDemoServiceImpl.java, v 0.1 2023年11月02日 15:04 syd
 */
public class SecondDemoServiceImpl implements DemoService {
    @Override
    public DemoResponse handle(DemoRequest demoRequest) {
        DemoResponse demoResponse = new DemoResponse();
        demoResponse.setResult(demoRequest.getBiz() + "->" + getClass().getName());
        return demoResponse;
    }
}