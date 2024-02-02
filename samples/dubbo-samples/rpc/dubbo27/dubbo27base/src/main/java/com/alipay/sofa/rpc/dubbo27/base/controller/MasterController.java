package com.alipay.sofa.rpc.dubbo27.base.controller;

import com.alipay.sofa.rpc.dubbo27.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo27.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo27.model.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: yuanyuan
 * @date: 2023/12/22 4:12 下午
 */
@RestController
public class MasterController {

    @Resource
    private DemoService demoServiceRef;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public DemoResponse handle(String ref) {
        String appName = applicationContext.getId();
        DemoRequest demoRequest = new DemoRequest();
        demoRequest.setBiz(appName);
        return demoServiceRef.handle(demoRequest);
    }

    public void setDemoServiceRef(DemoService demoServiceRef) {
        this.demoServiceRef = demoServiceRef;
    }

}
