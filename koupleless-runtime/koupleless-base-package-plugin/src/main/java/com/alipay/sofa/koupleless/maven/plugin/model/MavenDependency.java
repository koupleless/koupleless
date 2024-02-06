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
public class MavenDependency {

    private String artifactId;

    private String groupId;

    private String version;
}
