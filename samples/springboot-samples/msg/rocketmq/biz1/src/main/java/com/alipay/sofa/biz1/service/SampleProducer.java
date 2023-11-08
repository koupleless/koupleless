package com.alipay.sofa.biz1.service;

import com.alipay.sofa.base.model.Greeting;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleProducer {

    private static Logger LOGGER = LoggerFactory.getLogger(SampleProducer.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/send/{input}")
    public String hello(@PathVariable String input) {
        String appName = applicationContext.getId();
        LOGGER.info("{} producer: {}", appName, input);
        input = appName + " send: " + input;
        Greeting greeting = new Greeting();
        greeting.setMessage(input);

        rocketMQTemplate.send("greeting-topic", new GenericMessage<>(greeting));
        return input;
    }
}
