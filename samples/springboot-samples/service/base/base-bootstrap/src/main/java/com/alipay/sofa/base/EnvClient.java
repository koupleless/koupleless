package com.alipay.sofa.base;

import org.springframework.stereotype.Service;

/**
 * @author: yuanyuan
 * @date: 2023/9/26 2:31 下午
 */
@Service
public class EnvClient {

    public String getEnv() {
        return "dev";
    }
}
