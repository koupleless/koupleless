package com.alipay.sofa.db.biz2.rest;

import com.alipay.sofa.db.base.infra.db.CRUDRepository;
import com.alipay.sofa.db.base.model.CommonModel;
import com.alipay.sofa.db.biz2.infra.db.UserRepository;
import com.alipay.sofa.db.biz2.model.User;
import com.alipay.sofa.serverless.common.api.AutowiredFromBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserRepository userRepository;

    @AutowiredFromBase
    private CRUDRepository commonModelRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getApplicationName();
        LOGGER.info("{} web test: into sample controller", appName);
        userRepository.findAll();
        return String.format("hello to %s deploy", appName);
    }

    @PostMapping(value = "/add")
    public User add(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping(value="/listUsers")
    public List<User> listOrders() {
        return (List<User>) userRepository.findAll();
    }

    @GetMapping(value = "/listCommons")
    public List<CommonModel> listCommons() {
        return (List<CommonModel>) commonModelRepository.findAll();
    }
}