package com.alipay.sofa.koupleless.test.suite.spring.mock.base;

import com.alipay.sofa.koupleless.test.suite.spring.mock.common.HelloService;
import org.springframework.stereotype.Component;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
@Component
public class SampleBaseService implements HelloService {
    public String helloWorld() {
        return getClass().getClassLoader().getClass().getName();
    }
}
