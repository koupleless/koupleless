package com.alipay.sofa.serverless.common.spring;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * @author: yuanyuan
 * @date: 2023/10/30 9:52 下午
 */
public class MasterBizPropertySource extends EnumerablePropertySource<Environment> {

    private final Set<String> keys;
    private final Environment environment;

    public MasterBizPropertySource(String name, @NonNull Environment environment, @NonNull Set<String> keys) {
        super(name, environment);
        this.environment = environment;
        this.keys = keys;
    }
    @Override
    public Object getProperty(String name) {
        return keys.contains(name) ? environment.getProperty(name) : null;
    }

    @Override
    public String[] getPropertyNames() {
        return StringUtils.toStringArray(keys);
    }
}
