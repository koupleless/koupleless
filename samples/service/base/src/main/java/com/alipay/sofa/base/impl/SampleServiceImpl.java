package com.alipay.sofa.base.impl;

import com.alipay.sofa.model.facade.SampleService;
import org.springframework.stereotype.Service;

@Service
public class SampleServiceImpl implements SampleService {

    @Override
    public String service() {
        return "A Sample Service";
    }

}
