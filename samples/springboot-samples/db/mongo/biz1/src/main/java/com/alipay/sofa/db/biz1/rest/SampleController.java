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
package com.alipay.sofa.db.biz1.rest;

import com.alipay.sofa.db.base.infra.db.CRUDRepository;
import com.alipay.sofa.db.base.model.CommonModel;
import com.alipay.sofa.db.biz1.infra.db.OrderRepository;
import com.alipay.sofa.db.biz1.model.Order;
import com.alipay.sofa.koupleless.common.api.AutowiredFromBase;
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
    private ApplicationContext  applicationContext;

    @Autowired
    private OrderRepository     orderRepository;

    @AutowiredFromBase
    private CRUDRepository      commonModelRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getId();
        LOGGER.info("{} web test: into sample controller", appName);
        orderRepository.findAll();
        return String.format("hello to %s deploy", appName);
    }

    @PostMapping(value = "/add")
    public Order add(@RequestBody Order order) {
        return orderRepository.save(order);
    }

    @GetMapping(value = "/listOrders")
    public List<Order> listOrders() {
        return (List<Order>) orderRepository.findAll();
    }

    @GetMapping(value = "/listCommons")
    public List<CommonModel> listCommons() {
        return (List<CommonModel>) commonModelRepository.findAll();
    }
}
