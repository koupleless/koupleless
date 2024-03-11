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
package com.alipay.sofa.koupleless.test.suite.spring.model;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class KouplelessBizSpringTestConfig {
    /**
     * 包名。
     */
    private String       packageName;

    /**
     * 业务名。
     */
    private String       bizName;

    /**
     * 主类。
     */
    private String       mainClass;

    /**
     * webContextPath。
     */
    private String       webContextPath;

    @Builder.Default
    private List<String> excludePackages = new ArrayList<>();

    public void init() {
        Preconditions.checkState(StringUtils.isNotBlank(packageName),
            "packageName must not be blank");

        if (StringUtils.isBlank(bizName)) {
            bizName = StringUtils.replace(packageName, ".", "-");
        }

        if (StringUtils.isBlank(mainClass)) {
            mainClass = packageName + ".Application";
        }

        if (StringUtils.isBlank(webContextPath)) {
            webContextPath = bizName;
        }
    }

}
