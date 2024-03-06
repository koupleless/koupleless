package spring.base;

import com.alipay.sofa.koupleless.plugin.spring.ServerlessEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author CodeNoobKing
 * @date 2024/3/6
 */
@Configuration
public class BaseConfiguration {
    @Bean
    public EnvironmentPostProcessor environmentPostProcessor() {
        return new ServerlessEnvironmentPostProcessor();
    }
}
