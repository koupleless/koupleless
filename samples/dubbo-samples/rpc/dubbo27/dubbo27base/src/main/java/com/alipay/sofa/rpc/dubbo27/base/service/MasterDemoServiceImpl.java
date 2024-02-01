package com.alipay.sofa.rpc.dubbo27.base.service;

import com.alipay.sofa.rpc.dubbo27.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo27.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo27.model.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author: yuanyuan
 * @date: 2023/12/22 11:39 上午
 */
public class MasterDemoServiceImpl implements DemoService {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public DemoResponse handle(DemoRequest demoRequest) {
        DemoResponse response = new DemoResponse();
        response.setResult(demoRequest.getBiz() + "->" + getClass().getName());
        return response;
    }
}
