package com.alipay.sofa.biz1.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private KafkaTemplate<Object, Object> template;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getApplicationName();
        LOGGER.info("{} web test: into sample controller", appName);
        return String.format("hello to %s deploy", appName);
    }

    @GetMapping("/send/{input}")
    public String send(@PathVariable String input) {
        template.send("topic_input_biz1", input);
        String appName = applicationContext.getId();
        LOGGER.info("{} producer test: into sample controller", appName);
        return String.format("hello to %s deploy", appName);
    }

    @KafkaListener(id = "biz1", topics = "topic_input_biz1")
    public void listen(String input) {
        String appName = applicationContext.getId();
        LOGGER.info("=================================");
        LOGGER.info("{} consumer input value: {}", appName, input);
        LOGGER.info("=================================");
    }
}
