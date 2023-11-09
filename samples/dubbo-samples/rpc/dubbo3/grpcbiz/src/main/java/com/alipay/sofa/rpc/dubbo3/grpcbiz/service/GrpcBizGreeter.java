/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo3.grpcbiz.service;

import java.util.concurrent.CompletableFuture;

import com.alipay.sofa.rpc.dubbo3.model.DubboGreeterTriple;
import com.alipay.sofa.rpc.dubbo3.model.GreeterReply;
import com.alipay.sofa.rpc.dubbo3.model.GreeterRequest;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author syd
 * @version GrpcBizGreeter.java, v 0.1 2023年11月06日 19:41 syd
 */
@DubboService(group = "grpcbiz")
public class GrpcBizGreeter extends DubboGreeterTriple.GreeterImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcBizGreeter.class);

    @Override
    public GreeterReply greet(GreeterRequest request) {
        LOGGER.info("Server {} received greet request {}", GrpcBizGreeter.class.getName(), request);
        return GreeterReply.newBuilder()
                .setMessage("hello," + request.getName())
                .build();
    }

    public CompletableFuture<GreeterReply> greetAsync(GreeterRequest request) {
        return CompletableFuture.completedFuture(greet(request));
    }
}