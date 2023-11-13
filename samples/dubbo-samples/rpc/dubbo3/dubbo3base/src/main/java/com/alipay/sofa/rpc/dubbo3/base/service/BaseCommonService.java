/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo3.base.service;

import com.alipay.sofa.rpc.dubbo3.model.CommonRequest;
import com.alipay.sofa.rpc.dubbo3.model.CommonResponse;
import com.alipay.sofa.rpc.dubbo3.model.CommonService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *
 * @author syd
 * @version BaseCommonService.java, v 0.1 2023年11月05日 16:05 syd
 */
@DubboService(group = "base")
public class BaseCommonService implements CommonService {
    @Override
    public CommonResponse sayHello(CommonRequest request) {
        CommonResponse response = new CommonResponse();
        response.setMessage(BaseCommonService.class.getName() + ": Hello," + request.getName());
        return response;
    }
}