package com.alipay.sofa.koupleless.test.suite.spring.framwork;

import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessSpringTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
public class KouplelessSpringTestUtils {
    private static AtomicBoolean              initialized = new AtomicBoolean(false);
    private static KouplelessSpringTestConfig config;

    public static void init() {
        if (initialized.compareAndSet(false, true)) {
            try {
                InputStream is = KouplelessSpringTestUtils.class.getClassLoader().getResourceAsStream(
                        "config/koupleless-test-framework-config.yaml"
                );
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                config = mapper.readValue(is, KouplelessSpringTestConfig.class);
            } catch (Throwable t) {
                initialized.set(false);
                throw new RuntimeException(t);
            }
        }
    }

    public static KouplelessSpringTestConfig getConfig() {
        init();
        return config;
    }
}
