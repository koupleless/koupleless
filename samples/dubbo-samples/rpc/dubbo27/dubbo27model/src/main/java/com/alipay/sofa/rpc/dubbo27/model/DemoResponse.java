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
 * @version DemoResponse.java, v 0.1 2023年10月31日 19:32 syd
 */
@Setter
@Getter
public class DemoResponse implements Serializable {
    private static final long serialVersionUID = 8278113695204981254L;
    private String result;
}