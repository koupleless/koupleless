package com.alipay.sofa.web.base.impl;

import com.alipay.sofa.web.base.facade.SampleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SampleServiceImpl implements SampleService {
    private static Logger LOGGER = LoggerFactory.getLogger(SampleServiceImpl.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public String service() {
        String appName = applicationContext.getId();

        LOGGER.info("{} web test: into a service", appName);
        return "A Sample Service";
    }
}
