package com.alipay.sofa.rpc.grpc.biz1.rest;

import com.alipay.sofa.rpc.grpc.model.pb.Greeter;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterRequest;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterResponse;
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

    @DubboReference(group = "grpc")
    private Greeter greeter;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getId();

        GreeterRequest greeterRequest = GreeterRequest.newBuilder().setName(appName).build();
        GreeterResponse response = greeter.greet(greeterRequest);
        log.info(response.getMessage());
        return response.getMessage();
    }
}
