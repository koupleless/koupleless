package com.alipay.sofa.logging.base.rest;

import com.alipay.sofa.logging.base.facade.SampleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
    private static Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SampleService sampleService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getId();
        LOGGER.info("{} web test: into sample controller", appName);

        sampleService.service();

        return String.format("hello to %s deploy", appName);
    }
}
