package com.alipay.sofa.koupleless.test.suite.spring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class KouplelessSpringTestConfig {
    private KouplelessSpringTestBizConfig biz;
}
