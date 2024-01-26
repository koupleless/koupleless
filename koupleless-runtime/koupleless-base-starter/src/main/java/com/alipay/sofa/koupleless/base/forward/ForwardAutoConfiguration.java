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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ConditionalOnProperty(name = "koupleless.forward.conf.path")
@ComponentScan(basePackages = "com.alipay.sofa.koupleless.base.forward")
public class ForwardAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext  applicationContext;

    private static final String EMPTY               = "";
    private static final String CONTEXT_PATH_PREFIX = "/";

    private static final String PATH_PREFIX         = "/";
    @Value("${koupleless.forward.conf.path}")
    private String              confPath;

    @Bean
    public Forwards forwards(ForwardItemComparator forwardItemComparator) throws IOException {
        List<Forward> forwards = fromYaml();
        List<ForwardItem> items = toForwardItems(forwards);
        items.sort(forwardItemComparator);
        return new Forwards(items);
    }

    @Bean
    @ConditionalOnMissingBean(ForwardItemComparator.class)
    public ForwardItemComparator forwardItemComparator() {
        return DefaultForwardItemComparator.getInstance();
    }

    private List<ForwardItem> toForwardItems(List<Forward> forwards) {
        if (CollectionUtils.isEmpty(forwards)) {
            return Collections.emptyList();
        }
        List<ForwardItem> items = new LinkedList<>();
        for (Forward forward : forwards) {
            items.addAll(toForwardItems(forward, items.size()));
        }
        return items;
    }

    private List<ForwardItem> toForwardItems(Forward forward, int startIndex) {
        Set<String> hosts = forward.getHosts();
        if (CollectionUtils.isEmpty(hosts)) {
            hosts = Collections.singleton(EMPTY);
        }
        String contextPath = forward.getContextPath();
        if (!contextPath.startsWith(CONTEXT_PATH_PREFIX)) {
            contextPath = CONTEXT_PATH_PREFIX + contextPath;
        }
        Set<String> paths = forward.getPaths();
        if (CollectionUtils.isEmpty(paths)) {
            paths = Collections.singleton(PATH_PREFIX);
        } else {
            paths = paths.stream()
                    .map(path -> path.startsWith(CONTEXT_PATH_PREFIX) ? path : CONTEXT_PATH_PREFIX + path)
                    .collect(Collectors.toSet());
        }
        List<ForwardItem> items = new LinkedList<>();
        int index = startIndex;
        for (String host : hosts) {
            for (String path : paths) {
                ForwardItem item = new ForwardItem(index++, contextPath, host, path);
                items.add(item);
            }
        }
        return items;
    }

    private List<Forward> fromYaml() throws IOException {
        Yaml yaml = new Yaml();
        Resource[] resources = applicationContext.getResources(confPath);
        List<Forward> forwards = new LinkedList<>();
        for (Resource resource : resources) {
            try (InputStream stream = resource.getInputStream()) {
                Object object = yaml.load(stream);
                JSONArray array = (JSONArray) JSON.toJSON(object);
                forwards.addAll(array.toJavaList(Forward.class));
            }
        }
        return forwards;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
