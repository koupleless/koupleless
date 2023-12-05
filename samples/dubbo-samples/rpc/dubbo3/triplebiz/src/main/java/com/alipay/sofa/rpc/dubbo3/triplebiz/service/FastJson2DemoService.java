/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo3.triplebiz.service;

import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoResponse;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *
 * @author syd
 * @version FastJson2DemoService.java, v 0.1 2023年11月08日 12:52 syd
 */
@DubboService(group = "fastjson2")
public class FastJson2DemoService implements DemoService {
    @Override
    public DemoResponse sayHello(DemoRequest request) {
        DemoResponse response = new DemoResponse();
        response.setMessage(FastJson2DemoService.class.getName() + ": Hello," + request.getName());
        return response;
    }
}