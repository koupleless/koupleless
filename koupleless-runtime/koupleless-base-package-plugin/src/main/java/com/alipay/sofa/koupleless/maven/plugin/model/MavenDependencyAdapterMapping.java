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
public class MavenDependencyAdapterMapping {
    /**
     * 匹配用户的以来。
     */
    private MavenDependencyMatcher matcher;

    /**
     * 适配的依赖。
     */
    private MavenDependency adapter;
}
