package com.alipay.sofa.web.biz2.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
public class SampleController {

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        String appName = applicationContext.getApplicationName();
        return String.format("hello to %s deploy", appName);
    }

    @RequestMapping(value = "/timestamp", method = RequestMethod.GET)
    public String timestamp() {
        String appName = applicationContext.getApplicationName();
        return String.format("%s now is %s", appName, LocalDateTime.now());
    }
}
