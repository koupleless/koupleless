package com.alipay.sofa.serverless.arklet.core.actuator.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Lunarscave
 */
public enum QueryType {
    /**
     * query system info
     */
    SYSTEM("system"),

    /**
     * query biz info
     */
    BIZ("biz"),

    /**
     * query plugin info
     */
    PLUGIN("plugin");

    private final String id;

    QueryType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static boolean containsId(String id) {
        List<QueryType> queryTypes = Arrays.asList(QueryType.class.getEnumConstants());
        return queryTypes.stream().map(queryType -> queryType.getId().equals(id))
                .reduce(false, (a, b) -> a || b);
    }
}
