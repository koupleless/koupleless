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
package com.alipay.sofa.koupleless.plugin.spring;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * @author: yuanyuan
 * @date: 2023/10/30 9:52 下午
 */
public class MasterBizPropertySource extends EnumerablePropertySource<Set<String>> {

    private final Set<String> keys;
    private final Environment environment;

    public MasterBizPropertySource(String name, @NonNull Environment environment,
                                   @NonNull Set<String> keys) {
        super(name, keys);
        this.environment = environment;
        this.keys = keys;
    }

    @Override
    public Object getProperty(String name) {
        return keys.contains(name) ? environment.getProperty(name) : null;
    }

    @Override
    public String[] getPropertyNames() {
        return StringUtils.toStringArray(keys);
    }
}
