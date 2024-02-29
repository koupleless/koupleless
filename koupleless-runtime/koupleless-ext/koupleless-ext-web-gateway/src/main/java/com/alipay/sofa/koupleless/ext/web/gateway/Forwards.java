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
package com.alipay.sofa.koupleless.ext.web.gateway;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Forwards {
    public static final String       ROOT_PATH      = "/";

    private static final String      PATH_SEPARATOR = "/";

    private static final String      HOST_SEPARATOR = ".";

    private List<ForwardItem>        items;
    private Map<String, ForwardItem> contextPathMap;

    public void setItems(List<ForwardItem> items) {
        this.items = items;
        this.contextPathMap = new ConcurrentHashMap<>();
    }

    public ForwardItem getForwardItem(String host, String path) {
        String key = host + path;
        return contextPathMap.computeIfAbsent(key, k -> doGetForwardItem(host, path));
    }

    private ForwardItem doGetForwardItem(String host, String path) {
        for (ForwardItem item : items) {
            //host完全相同，规则没有限制host，host以$form + "." 开头，均视为域名匹配
            boolean matchHost = !StringUtils.hasLength(item.getHost())
                                || Objects.equals(item.getHost(), host)
                                || host.startsWith(item.getHost() + HOST_SEPARATOR);
            //path完全相同，规则没有限制path，host以$form + "/" 开头，均视为路径匹配
            boolean matchPath = Objects.equals(path, item.getFrom())
                                || Objects.equals(ROOT_PATH, item.getFrom())
                                || path.startsWith(item.getFrom() + PATH_SEPARATOR);
            if (matchHost && matchPath) {
                return item;
            }
        }
        return null;
    }
}
