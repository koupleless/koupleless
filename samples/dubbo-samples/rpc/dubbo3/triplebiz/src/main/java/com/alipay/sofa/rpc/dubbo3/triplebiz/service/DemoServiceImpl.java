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
 * @version DemoServiceImpl.java, v 0.1 2023年11月05日 15:21 syd
 */
@DubboService(group = "triplebiz")
public class DemoServiceImpl implements DemoService {
    @Override
    public DemoResponse sayHello(DemoRequest request) {
        DemoResponse response = new DemoResponse();
        response.setMessage(DemoServiceImpl.class.getName() + ": Hello," + request.getName());
        return response;
    }
}