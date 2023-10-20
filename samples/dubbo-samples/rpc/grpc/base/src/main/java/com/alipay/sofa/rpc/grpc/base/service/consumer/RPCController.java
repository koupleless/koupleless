package com.alipay.sofa.rpc.grpc.base.service.consumer;

import com.alipay.sofa.rpc.grpc.base.model.PoJoModel;
import com.alipay.sofa.rpc.grpc.base.model.PoJoModelService;
import com.alipay.sofa.rpc.grpc.model.pb.GreeterRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RPCController {

    @DubboReference(group = "triplePojo", scope = "remote")
    private PoJoModelService poJoModelService;

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping(value = "/")
    public String hello() {
        String appName = applicationContext.getId();

        GreeterRequest greeterRequest = GreeterRequest.newBuilder().setName(appName).build();

        PoJoModel poJoModel = new PoJoModel();
        poJoModel.setName(appName);
        PoJoModel response = poJoModelService.revert(poJoModel);
        log.info(response.getName());
        return response.getName();
    }
}
