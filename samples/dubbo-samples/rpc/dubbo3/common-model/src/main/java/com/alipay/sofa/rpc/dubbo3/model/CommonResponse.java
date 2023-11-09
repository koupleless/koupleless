/*
 * Ant Group
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.rpc.dubbo3.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author syd
 * @version DemoResponse.java, v 0.1 2023年11月05日 15:17 syd
 */
@Getter
@Setter
public class CommonResponse implements Serializable {
    private static final long serialVersionUID = -6796302417012125318L;
    private String message;
}