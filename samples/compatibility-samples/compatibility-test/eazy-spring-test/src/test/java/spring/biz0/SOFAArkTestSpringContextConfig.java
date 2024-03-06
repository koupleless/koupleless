package spring.biz0;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CodeNoobKing
 * @date 2024/3/6
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SOFAArkTestSpringContextConfig {
    /**
     * 包名。
     */
    private String packageName;

    /**
     * 主类。
     */
    private String scannerClass;

    /**
     * 模块名。
     */
    private String moduleName;
}
