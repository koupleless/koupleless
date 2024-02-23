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

import com.alipay.sofa.biz.facade.Param;
import com.alipay.sofa.biz.facade.Provider;
import com.alipay.sofa.biz.facade.Result;
import com.alipay.sofa.koupleless.common.api.AutowiredFromBiz;
import com.alipay.sofa.koupleless.common.api.SpringServiceFinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @className: SampleController
 * @author: yuanyuan
 * @description:
 * @date: 2023/6/29 8:47 下午
 */
@RestController
public class SampleController {

    @AutowiredFromBiz(bizName = "biz1", bizVersion = "0.0.1-SNAPSHOT", name = "studentProvider")
    private Provider studentProvider;

    @AutowiredFromBiz(bizName = "biz1", name = "teacherProvider")
    private Provider teacherProvider;


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        Result tmp = studentProvider.provide(new Param());
        System.out.println(tmp.getClass());
        System.out.println(tmp.isSuccess());
        System.out.println(tmp.getPeople().getClass());
        System.out.println(tmp);

        Result tmp1 = teacherProvider.provide(new Param());
        System.out.println(tmp1.getClass());
        System.out.println(tmp1.isSuccess());
        System.out.println(tmp1.getPeople().getClass());
        System.out.println(tmp1);

        Provider studentProvider = SpringServiceFinder.getModuleService("biz1", "0.0.1-SNAPSHOT",
            "studentProvider", Provider.class);
        Result result = studentProvider.provide(new Param());
        System.out.println(result.getClass());
        System.out.println(result.isSuccess());
        System.out.println(result.getPeople().getClass());
        System.out.println(result);

        Provider teacherProvider = SpringServiceFinder.getModuleService("biz1", "0.0.1-SNAPSHOT",
            "teacherProvider", Provider.class);
        Result result1 = teacherProvider.provide(new Param());
        System.out.println(result1.getClass());
        System.out.println(result1.isSuccess());
        System.out.println(result1.getPeople().getClass());
        System.out.println(result1);

        Map<String, Provider> providerMap = SpringServiceFinder.listModuleServices("biz1",
            "0.0.1-SNAPSHOT", Provider.class);
        for (String beanName : providerMap.keySet()) {
            Result result2 = providerMap.get(beanName).provide(new Param());
            System.out.println(result2.getClass());
        }

        return "hello to ark master biz";
    }
}
