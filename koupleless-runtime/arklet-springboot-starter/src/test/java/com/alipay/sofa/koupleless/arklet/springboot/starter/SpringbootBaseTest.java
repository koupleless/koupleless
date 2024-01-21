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
package com.alipay.sofa.koupleless.arklet.springboot.starter;

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Lunarscave
 */
public class SpringbootBaseTest {

    @BeforeClass
    public static void initSpringbootTest() {
        ClassLoaderUtils.pushContextClassLoader(ClassLoader.getSystemClassLoader());
        System.setProperty(Constants.EMBED_ENABLE, "true");
        BaseSpringApplication.main(new String[] {});
    }

    @AfterClass
    public static void destroySpringbootTest() {
        System.setProperty(Constants.EMBED_ENABLE, "");
    }
}
