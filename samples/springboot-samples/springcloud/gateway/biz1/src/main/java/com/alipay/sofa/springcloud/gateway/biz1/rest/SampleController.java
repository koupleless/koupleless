package com.alipay.sofa.springcloud.gateway.biz1.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getApplicationName();
        return String.format("hello to %s deploy", appName);
    }

    @RequestMapping("/circuitbreakerfallback")
    public String circuitbreakerfallback() {
        return "This is a fallback";
    }
}
