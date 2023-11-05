/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo26.model;

/**
 *
 * @author syd
 * @version HelloService.java, v 0.1 2023年11月02日 14:57 syd
 */
public interface HelloService {
    /**
     *
     * @param request
     * @return
     */
    HelloResponse sayHello(HelloRequest request);
}