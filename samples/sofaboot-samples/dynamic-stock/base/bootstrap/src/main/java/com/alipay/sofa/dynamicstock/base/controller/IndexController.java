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
package com.alipay.sofa.dynamicstock.base.controller;

import com.alipay.sofa.dynamicstock.base.data.DatabaseSeed;
import com.alipay.sofa.dynamicstock.base.facade.StrategyService;
import com.alipay.sofa.dynamicstock.base.model.ProductInfo;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * index controller
 *
 * @author caojie.cj@antfin.com
 * @since 2020/2/11
 */
@Controller
public class IndexController {
    @SofaReference
    private StrategyService strategyService;

    @RequestMapping("/")
    public String index(Model model) {
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
