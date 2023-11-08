/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo3.triplebiz.service;

import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoResponse;

/**
 *
 * @author syd
 * @version DemoService.java, v 0.1 2023年11月05日 15:17 syd
 */
public interface DemoService {

    DemoResponse sayHello(DemoRequest request);
}