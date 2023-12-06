/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo27.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author syd
 * @version HelloResponse.java, v 0.1 2023年11月02日 14:58 syd
 */
@Getter
@Setter
public class HelloResponse implements Serializable {
    private static final long serialVersionUID = 1833679835915371983L;
    private String data;
}