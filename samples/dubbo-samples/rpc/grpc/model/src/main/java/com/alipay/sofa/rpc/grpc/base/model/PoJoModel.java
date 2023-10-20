/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.grpc.base.model;

import java.io.Serializable;

/**
 *
 * @author syd
 * @version PoJoModel.java, v 0.1 2023年10月20日 15:10 syd
 */
public class PoJoModel implements Serializable {
    private static final long serialVersionUID = -7689572111111111L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}