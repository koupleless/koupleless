package com.alipay.sofa.koupleless.maven.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CodeNoobKing
 * @date 2024/2/6
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MavenDependencyMatcher {
    /**
     * 用正则表达式匹配用户的依赖。
     */
    private String regexp;
}
