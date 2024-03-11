package com.alipay.sofa.koupleless.test.suite.spring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class KouplelessMultiSpringTestConfig {

    private KouplelessBaseSpringTestConfig baseConfig;

    private List<KouplelessBizSpringTestConfig> bizConfigs;

    public void init() {
        baseConfig.init();
        if (bizConfigs == null) {
            bizConfigs = new ArrayList<>();
        }
        for (KouplelessBizSpringTestConfig bizConfig : bizConfigs) {
            bizConfig.init();
        }
    }
}

