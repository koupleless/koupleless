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
package com.alipay.sofa.serverless.log4j2.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ruoshan
 * @version $Id: Log2Print.java, v 0.1 2018年10月16日 5:49 PM ruoshan Exp $
 */
public class Log2Print {

    public static final String PATH_KEY = "logging.path2";

    private final Logger       logger   = LoggerFactory.getLogger(Log2Print.class);

    public void printLog() {
        logger.error("log2");
    }
}