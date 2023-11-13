package com.alipay.sofa.base.service;

import com.alipay.sofa.base.model.Greeting;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
        topic = "greeting-topic",
        consumerGroup = "base-group-add-topic"
)
public class SampleConsumer implements RocketMQListener<Greeting> {
    private static Logger LOGGER = LoggerFactory.getLogger(SampleConsumer.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onMessage(Greeting greeting) {
        String appName = applicationContext.getId();

        LOGGER.info("=================================");
        LOGGER.info("{} receive a message: {}", appName, greeting);
    }
}
