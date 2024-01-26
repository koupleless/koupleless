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

import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Forwards {
    private final List<ForwardItem>   items;
    private final Map<String, String> contextPathMap = new ConcurrentHashMap<>();

    public Forwards(List<ForwardItem> items) {
        this.items = items;
    }

    public String getContextPath(URI uri) {
        String host = uri.getHost();
        String path = uri.getPath();
        String key = host + path;
        return contextPathMap.computeIfAbsent(key, k -> parseContextPath(host, path));
    }

    private String parseContextPath(String host, String path) {
        for (ForwardItem item : items) {
            boolean matchHost = !StringUtils.hasLength(item.getHost())
                                || host.startsWith(item.getHost());
            boolean matchPath = path.startsWith(item.getPath());
            if (matchHost && matchPath) {
                return item.getContextPath();
            }
        }
        throw new IllegalArgumentException("Not found context path by the path:" + host + path);
    }
}
