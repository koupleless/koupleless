package com.alipay.sofa.rpc.grpc.base.service.service;

import com.alipay.sofa.rpc.grpc.model.pb.DubboGreeterTriple;
import com.alipay.sofa.rpc.grpc.model.pb.Greeter;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterRequest;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(group = "triple")
@Slf4j
public class TripleIGreetermpl extends DubboGreeterTriple.GreeterImplBase implements Greeter {

    @Override
    public GreeterResponse greet(GreeterRequest request) {
        log.info("Receive request ======> {}", request.getName());
        GreeterResponse response = GreeterResponse.newBuilder().setMessage("Hello " + request.getName()).build();
        return response;
    }
}