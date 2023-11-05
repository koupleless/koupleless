package com.alipay.sofa.rpc.grpc.base.service.service;

import com.alipay.sofa.rpc.grpc.model.pb.Greeter;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterGrpc;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterRequest;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(group = "grpc")
@Slf4j
public class GrpcGreetermpl extends GreeterGrpc.GreeterImplBase implements Greeter {

    @Override
    public GreeterResponse greet(GreeterRequest request) {
        log.info("Receive request ======> {}", request.getName());
        GreeterResponse response = GreeterResponse.newBuilder().setMessage("Hello " + request.getName()).build();
        return response;
    }
}