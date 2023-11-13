package com.alipay.sofa.web.base.impl;

import com.alipay.sofa.web.base.facade.SampleService;
import org.springframework.stereotype.Service;

@Service
public class SampleServiceImpl implements SampleService {

    @Override
    public String service() {
        return "A Sample Service";
    }
}
