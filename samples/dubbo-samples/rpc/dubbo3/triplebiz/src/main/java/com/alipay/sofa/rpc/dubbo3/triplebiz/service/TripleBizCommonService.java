/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo3.triplebiz.service;

import com.alipay.sofa.rpc.dubbo3.model.CommonRequest;
import com.alipay.sofa.rpc.dubbo3.model.CommonResponse;
import com.alipay.sofa.rpc.dubbo3.model.CommonService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *  jvm服务，可以被基座调用
 * @author syd
 * @version TripleBizCommonService.java, v 0.1 2023年11月05日 16:02 syd
 */
@DubboService(group = "triplebiz")
public class TripleBizCommonService implements CommonService {
    @Override
    public CommonResponse sayHello(CommonRequest request) {
        CommonResponse response = new CommonResponse();
        response.setMessage(TripleBizCommonService.class.getName() + ": Hello," + request.getName());
        return response;
    }
}