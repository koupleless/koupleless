package com.alipay.sofa.base.rest;

import com.alipay.sofa.base.facade.SampleService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    private static Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Resource
    private KafkaTemplate<Object, Object> template;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SampleService sampleService;

    @GetMapping("/send/{input}")
    public String hello(@PathVariable String input) {
        this.template.send("topic_input_base", input);

        String appName = applicationContext.getId();
        LOGGER.info("{} producer test: into sample controller", appName);
        sampleService.service();
        return String.format("hello to %s deploy", appName);
    }

    @KafkaListener(id = "base", topics = "topic_input_base")
    public void listen(String input) {
        String appName = applicationContext.getId();
        LOGGER.info("=================================");
        LOGGER.info("{} consumer input value: {}", appName, input);
        LOGGER.info("=================================");
    }
}
