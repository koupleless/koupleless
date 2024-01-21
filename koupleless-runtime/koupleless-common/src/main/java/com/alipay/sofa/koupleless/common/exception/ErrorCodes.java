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
package com.alipay.sofa.koupleless.common.exception;

public class ErrorCodes {

    public static class SpringContextManager {
        /**
         * 模块 SpringContext 关闭失败
         */
        public static final String E100001 = "100001";

        /**
         * 模块 SpringContext 找不到
         */
        public static final String E100002 = "100002";

        /**
         * 模块为空
         */
        public static final String E100003 = "100003";

        /**
         * 模块状态非法
         */
        public static final String E100004 = "100004";

        /**
         * 跨模块调用，目标biz找不到该类
         */
        public static final String E100005 = "100005";
    }
}
