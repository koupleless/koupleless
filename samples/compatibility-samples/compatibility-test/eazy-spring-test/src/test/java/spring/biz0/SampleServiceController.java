package spring.biz0;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CodeNoobKing
 * @date 2024/3/6
 */
@RestController
public class SampleServiceController {

    @GetMapping(value = "/hello")
    public String hello() {
        return getClass().getClassLoader().getClass().getName();
    }
}
