/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.grpc.base.service.service;

import com.alipay.sofa.rpc.grpc.base.model.PoJoModel;
import com.alipay.sofa.rpc.grpc.base.model.PoJoModelService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

/**
 *
 * @author syd
 * @version TriplePoJoModelServiceImpl.java, v 0.1 2023年10月20日 15:11 syd
 */
@DubboService(group = "triplePojo")
public class TriplePoJoModelServiceImpl implements PoJoModelService {
    @Override
    public PoJoModel revert(PoJoModel poJoModel) {
        PoJoModel newPoJoModel = new PoJoModel();
        newPoJoModel.setName(StringUtils.reverse(poJoModel.getName()));
        return newPoJoModel;
    }
}