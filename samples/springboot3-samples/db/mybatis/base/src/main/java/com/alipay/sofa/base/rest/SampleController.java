package com.alipay.sofa.base.rest;

import com.alipay.sofa.base.facade.SampleService;
import com.alipay.sofa.base.mapper.UserMapper;
import com.alipay.sofa.base.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class SampleController {

    private static Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/hello/{input}")
    public String hello(@PathVariable String input) {
        sampleService.service();
        return String.format("hello %s to %s deploy", input, applicationContext.getId());
    }

    @GetMapping("/mybatis")
    public List<User> mybatis() {
        return userMapper.getAll();
    }
}
