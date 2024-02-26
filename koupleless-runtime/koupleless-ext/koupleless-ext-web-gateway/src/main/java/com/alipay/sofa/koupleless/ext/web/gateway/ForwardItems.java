package com.alipay.sofa.koupleless.ext.web.gateway;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ForwardItems {
    private static final String EMPTY = "";
    private static final String CONTEXT_PATH_PREFIX = "/";

    private static final String PATH_PREFIX = "/";

    public static void init(Forwards bean, GatewayProperties gatewayProperties) {
        //load conf
        List<Forward> forwards = gatewayProperties.getForwards();
        List<ForwardItem> items = toForwardItems(forwards);
        items.sort(ForwardItemComparator.getInstance());
        //make forwards bean and set conf
        bean.setItems(items);
    }

    private static List<ForwardItem> toForwardItems(List<Forward> forwards) {
        if (CollectionUtils.isEmpty(forwards)) {
            return Collections.emptyList();
        }
        List<ForwardItem> items = new LinkedList<>();
        for (Forward forward : forwards) {
            items.addAll(toForwardItems(forward));
        }
        return items;
    }

    private static List<ForwardItem> toForwardItems(Forward forward) {
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
}
