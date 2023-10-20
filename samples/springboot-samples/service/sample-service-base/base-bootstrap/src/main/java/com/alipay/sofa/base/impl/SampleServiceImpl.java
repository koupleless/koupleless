package com.alipay.sofa.base.impl;

import com.alipay.sofa.base.facade.SampleService;
import org.springframework.stereotype.Service;

@Service
public class SampleServiceImpl implements SampleService {

    @Override
    public String service() {
        return "A Sample Service";
    }

}
