/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo26.base.service;

import com.alipay.sofa.rpc.dubbo26.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo26.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo26.model.DemoService;

/**
 *
 * @author syd
 * @version BaseDemoService.java, v 0.1 2023年11月06日 12:21 syd
 */
public class BaseDemoService implements DemoService {

    @Override
    public DemoResponse handle(DemoRequest demoRequest) {
        DemoResponse response = new DemoResponse();
        response.setResult(demoRequest.getBiz() + "->" + getClass().getName());
        return response;
    }
}