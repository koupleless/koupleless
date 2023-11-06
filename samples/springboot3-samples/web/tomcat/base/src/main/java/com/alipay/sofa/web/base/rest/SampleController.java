package com.alipay.sofa.web.base.rest;

import com.alipay.sofa.web.base.facade.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SampleService sampleService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getId();

        sampleService.service();

        return String.format("hello to %s deploy", appName);
    }
}
