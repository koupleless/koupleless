package spring.biz0;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
     * 业务名。
     */
    private String bizName;

    /**
     * 主类。
     */
    private String mainClass;

    public void init() {
        Preconditions.checkState(
                StringUtils.isNotBlank(packageName),
                "packageName must not be blank"
        );

        if (StringUtils.isBlank(bizName)) {
            bizName = StringUtils.replace(packageName, ".", "-");
        }

        if (StringUtils.isBlank(mainClass)) {
            mainClass = packageName + ".Application.class";
        }
    }

}
