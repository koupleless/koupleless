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
 * @version DemoRequest.java, v 0.1 2023年10月31日 19:32 syd
 */
@Setter
@Getter
public class DemoRequest implements Serializable {
    private static final long serialVersionUID = 6104442378964939532L;

    private String biz;
}