package com.alipay.sofa.web.biz1.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getApplicationName();
        LOGGER.info("{} web test: into sample controller", appName);
        return String.format("hello to %s deploy", appName);
    }
}
