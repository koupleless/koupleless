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
public class KouplelessSpringTestBizConfig {
    @Builder.Default
    private List<String> excludeDependencyRegexps = new ArrayList<>();
}
