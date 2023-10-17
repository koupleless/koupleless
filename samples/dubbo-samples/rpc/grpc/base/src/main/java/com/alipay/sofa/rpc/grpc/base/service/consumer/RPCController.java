package com.alipay.sofa.rpc.grpc.base.service.consumer;

import com.alipay.sofa.rpc.grpc.model.pb.DubboGreeterTriple;
import com.alipay.sofa.rpc.grpc.model.pb.Greeter;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterRequest;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RPCController {

    @DubboReference(proxy = CommonConstants.NATIVE_STUB, group = "grpc")
    private Greeter greeter;

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping(value = "/")
    public String hello() {
        String appName = applicationContext.getId();

        GreeterRequest greeterRequest = GreeterRequest.newBuilder().setName(appName).build();

        // 调用这个接口会报错，原因在这里 https://github.com/apache/dubbo/pull/13200
        // 这个接口可以先不验证
        GreeterResponse response = greeter.greet(greeterRequest);
        log.info(response.getMessage());
        return response.getMessage();
    }
}
