package com.alipay.sofa.serverless.common.util;

import java.util.Collections;
import java.util.HashSet;

/**
 * @author: yuanyuan
 * @date: 2023/10/30 9:49 下午
 */
public class CollectionUtils {

    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<>();
        Collections.addAll(set, elements);
        return set;
    }
}
