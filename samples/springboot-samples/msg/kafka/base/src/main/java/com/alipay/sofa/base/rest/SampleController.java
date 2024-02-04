/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.base.rest;

import com.alipay.sofa.base.facade.SampleService;
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

    private static Logger                 LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private KafkaTemplate<Object, Object> template;

    @Autowired
    private ApplicationContext            applicationContext;

    @Autowired
    private SampleService                 sampleService;

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
