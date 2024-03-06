package spring.biz0;

import org.springframework.stereotype.Component;

/**
 * @author CodeNoobKing
 * @date 2024/3/6
 */
@Component
public class SampleService {

    public String hellyWorld() {
        return getClass().getClassLoader().getClass().getName();
    }
}
