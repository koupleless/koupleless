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
package com.alipay.sofa.koupleless.base.forward;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 该Java函数用于比较两个ForwardItem对象，通过比较它们的host和path属性长度决定优先级。
 * 若item1无host而item2有，则item2优先；反之item1优先。
 * 然后根据path长度差值判断，若相等则再依据host长度差值确定返回值（即优先级）。
 */
@ConditionalOnMissingBean(ForwardItemComparator.class)
@Component
class DefaultForwardItemComparator implements ForwardItemComparator {
    @Override
    public int compare(ForwardItem item1, ForwardItem item2) {
        if (!StringUtils.hasLength(item1.getHost()) && StringUtils.hasLength(item2.getHost())) {
            return 1;
        }
        if (StringUtils.hasLength(item1.getHost()) && !StringUtils.hasLength(item2.getHost())) {
            return -1;
        }
        int num = item2.getPath().length() - item1.getPath().length();
        if (num == 0) {
            num = item2.getHost().length() - item1.getHost().length();
        }
        return num;
    }
}
