package com.alipay.sofa.db.base.rest;

import com.alipay.sofa.db.base.infra.db.CommonModelRepository;
import com.alipay.sofa.db.base.model.CommonModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
    private static Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CommonModelRepository commonModelRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getId();
        LOGGER.info("{} web test: into sample controller", appName);
        return String.format("hello to %s deploy", appName);
    }

    @PostMapping(value = "/add")
    public CommonModel add(@RequestBody CommonModel commonModel) {
        return commonModelRepository.save(commonModel);
    }
}
