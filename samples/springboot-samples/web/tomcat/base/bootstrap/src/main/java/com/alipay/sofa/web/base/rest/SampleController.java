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
package com.alipay.sofa.web.base.rest;

import com.alipay.sofa.dynamicstock.base.facade.StrategyService;
import com.alipay.sofa.dynamicstock.base.model.ProductInfo;
import com.alipay.sofa.koupleless.common.api.SpringServiceFinder;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import com.alipay.sofa.web.base.data.DatabaseSeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SampleController {

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/")
    @ResponseBody
    public String hello() {
        String appName = applicationContext.getId();
        return String.format("hello to %s deploy", appName);
    }

    @RequestMapping(value = "/order1", method = RequestMethod.GET)
    public String biz1(Model model) {
        StrategyService strategyService;
        try {
            strategyService = SpringServiceFinder.getModuleService("biz1-web-single-host",
                "0.0.1-SNAPSHOT", "strategyServiceImpl", StrategyService.class);
        } catch (BizRuntimeException e) {
            model.addAttribute("appName", applicationContext.getId());
            return "index";
        }

        model.addAttribute("appName", strategyService.getAppName());
        model.addAttribute("productList", strategyService.strategy(initProducts()));
        return "index";
    }

    @RequestMapping(value = "/order2", method = RequestMethod.GET)
    public String biz2(Model model) {
        StrategyService strategyService;
        try {
            strategyService = SpringServiceFinder.getModuleService("biz2-web-single-host",
                "0.0.1-SNAPSHOT", "strategyServiceImpl", StrategyService.class);
        } catch (BizRuntimeException e) {
            model.addAttribute("appName", applicationContext.getId());
            return "index";
        }

        if (strategyService == null) {
            return "index";
        }

        model.addAttribute("appName", strategyService.getAppName());
        model.addAttribute("productList", strategyService.strategy(initProducts()));
        return "index";
    }

    /**
     * 初始化默认展示列表,为了实验效果，此处初始化的列表与实际列表是相反的，但是实际排序结果与现场购买订单直接挂钩
     *
     * @return
     */
    private List<ProductInfo> initProducts() {
        List<ProductInfo> products = new ArrayList<>(5);
        for (int i = 4; i >= 0; i--) {
            ProductInfo productInfo = new ProductInfo();
            productInfo.setName(DatabaseSeed.name[i]);
            productInfo.setOrderCount(DatabaseSeed.orderCount[i]);
            productInfo.setSrc(DatabaseSeed.imageUrls[i]);
            productInfo.setAuthor(DatabaseSeed.authors[i]);
            products.add(productInfo);
        }
        return products;
    }
}
