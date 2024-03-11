package com.alipay.sofa.koupleless.test.suite.spring.model;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
public class KouplelessBizSpringTestConfig {
    /**
     * 包名。
     */
    private String packageName;

    /**
     * 业务名。
     */
    private String bizName;

    /**
     * 主类。
     */
    private String mainClass;

    /**
     * webContextPath。
     */
    private String webContextPath;

    @Builder.Default
    private List<String> excludePackages = new ArrayList<>();

    public void init() {
        Preconditions.checkState(
                StringUtils.isNotBlank(packageName),
                "packageName must not be blank"
        );

        if (StringUtils.isBlank(bizName)) {
            bizName = StringUtils.replace(packageName, ".", "-");
        }

        if (StringUtils.isBlank(mainClass)) {
            mainClass = packageName + ".Application";
        }

        if (StringUtils.isBlank(webContextPath)) {
            webContextPath = bizName;
        }
    }

}
