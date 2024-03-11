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
package com.alipay.sofa.koupleless.test.suite.spring.framwork;

import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessSpringTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
public class KouplelessSpringTestUtils {
    private static AtomicBoolean              initialized = new AtomicBoolean(false);
    private static KouplelessSpringTestConfig config;

    public static void init() {
        if (initialized.compareAndSet(false, true)) {
            try {
                InputStream is = KouplelessSpringTestUtils.class.getClassLoader()
                    .getResourceAsStream("config/koupleless-test-framework-config.yaml");
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                config = mapper.readValue(is, KouplelessSpringTestConfig.class);
            } catch (Throwable t) {
                initialized.set(false);
                throw new RuntimeException(t);
            }
        }
    }

    public static KouplelessSpringTestConfig getConfig() {
        init();
        return config;
    }
}
