package com.alipay.sofa.rpc.dubbo3.grpcbiz.rest;

import org.apache.dubbo.common.constants.CommonConstants;
import com.alipay.sofa.rpc.dubbo3.model.Greeter;
import com.alipay.sofa.rpc.dubbo3.model.GreeterReply;
import com.alipay.sofa.rpc.dubbo3.model.GreeterRequest;
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

    @DubboReference(group = "grpcbiz", scope = "remote",proxy = CommonConstants.NATIVE_STUB)
    private Greeter greeter;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getId();

        GreeterRequest greeterRequest = GreeterRequest.newBuilder().setName(appName).build();
        GreeterReply response = greeter.greet(greeterRequest);
        log.info(response.getMessage());
        return response.getMessage();
    }
}
