/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        return GreeterReply.newBuilder().setMessage("hello," + request.getName()).build();
    }

    public CompletableFuture<GreeterReply> greetAsync(GreeterRequest request) {
        return CompletableFuture.completedFuture(greet(request));
    }
}