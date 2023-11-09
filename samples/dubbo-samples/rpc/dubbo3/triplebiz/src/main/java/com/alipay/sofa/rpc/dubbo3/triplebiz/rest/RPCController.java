package com.alipay.sofa.rpc.dubbo3.triplebiz.rest;

import com.alipay.sofa.rpc.dubbo3.model.CommonRequest;
import com.alipay.sofa.rpc.dubbo3.model.CommonResponse;
import com.alipay.sofa.rpc.dubbo3.model.CommonService;
import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoRequest;
import com.alipay.sofa.rpc.dubbo3.triplebiz.model.DemoResponse;
import com.alipay.sofa.rpc.dubbo3.triplebiz.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RPCController {

    /**
     * tri协议，远程调用，默认走hessian序列化
     */
    @DubboReference(group = "triplebiz", scope = "remote")
    private DemoService demoService;

    /**
     * tri协议，injvm调用，scope默认走injvm
     */
    @DubboReference(group = "base")
    private CommonService commonServiceInJvm;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/remote", method = RequestMethod.GET)
    public String remote() {
        try {
            String appName = applicationContext.getId();
            DemoRequest demoRequest = new DemoRequest();
            demoRequest.setName(appName);
            DemoResponse response = demoService.sayHello(demoRequest);
            log.info(response.getMessage());
            return response.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/injvm", method = RequestMethod.GET)
    public String injvm() {
        try {
            String appName = applicationContext.getId();
            CommonRequest demoRequest = new CommonRequest();
            demoRequest.setName(appName);
            CommonResponse response = commonServiceInJvm.sayHello(demoRequest);
            log.info(response.getMessage());
            return response.getMessage();
        } catch (Exception e){
            return e.getMessage();
        }
    }
}
