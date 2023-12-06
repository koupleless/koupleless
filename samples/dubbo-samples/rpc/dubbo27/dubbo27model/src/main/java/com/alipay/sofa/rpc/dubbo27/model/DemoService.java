/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo27.model;

/**
 *
 * @author syd
 * @version DemoService.java, v 0.1 2023年10月31日 19:32 syd
 */
public interface DemoService {
    /**
     *
     * @param demoRequest
     * @return
     */
    DemoResponse handle(DemoRequest demoRequest);
}