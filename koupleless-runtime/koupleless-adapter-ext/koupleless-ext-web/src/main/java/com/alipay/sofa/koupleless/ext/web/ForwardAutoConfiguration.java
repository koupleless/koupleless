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
package com.alipay.sofa.koupleless.ext.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Value;
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

@ConditionalOnProperty(name = "koupleless.forward.conf.path")
@ComponentScan(basePackages = "com.alipay.sofa.koupleless.ext.web")
public class ForwardAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext  applicationContext;

    private static final String EMPTY               = "";
    private static final String CONTEXT_PATH_PREFIX = "/";

    private static final String PATH_PREFIX         = "/";
    @Value("${koupleless.forward.conf.path}")
    private String              confPath;

    @Bean
    public Forwards forwards() throws IOException {
        //load conf
        List<Forward> forwards = fromYaml();
        List<ForwardItem> items = toForwardItems(forwards);
        items.sort(ForwardItemComparator.getInstance());
        //make forwards bean and set conf
        Forwards bean = new Forwards();
        bean.setItems(items);
        return bean;
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
        Set<ForwardPath> paths = forward.getPaths();
        if (CollectionUtils.isEmpty(paths)) {
            ForwardPath path = new ForwardPath();
            path.setFrom(PATH_PREFIX);
            path.setTo(PATH_PREFIX);
            paths = Collections.singleton(path);
        } else {
            for (ForwardPath path : paths) {
                String from = path.getFrom();
                if (!from.startsWith(CONTEXT_PATH_PREFIX)) {
                    path.setFrom(CONTEXT_PATH_PREFIX + from);
                }
                String to = path.getTo();
                if (to == null) {
                    path.setTo(path.getFrom());
                    continue;
                }
                if (!to.startsWith(CONTEXT_PATH_PREFIX)) {
                    path.setTo(CONTEXT_PATH_PREFIX + to);
                }
            }
        }
        List<ForwardItem> items = new LinkedList<>();
        for (String host : hosts) {
            for (ForwardPath path : paths) {
                ForwardItem item = new ForwardItem(contextPath, host, path.getFrom(), path.getTo());
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
