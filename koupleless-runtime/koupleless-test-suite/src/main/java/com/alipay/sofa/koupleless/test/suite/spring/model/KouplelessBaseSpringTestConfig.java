package com.alipay.sofa.koupleless.test.suite.spring.model;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class KouplelessBaseSpringTestConfig {

    private String packageName;

    private String mainClass;

    public void init() {
        Preconditions.checkState(
                StringUtils.isNotBlank(packageName),
                "packageName must not be blank"
        );

        if (StringUtils.isBlank(mainClass)) {
            mainClass = packageName + ".Application";
        }
    }
}
