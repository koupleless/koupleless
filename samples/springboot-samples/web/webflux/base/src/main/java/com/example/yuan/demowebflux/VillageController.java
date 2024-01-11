package com.example.yuan.demowebflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author: yuanyuan
 * @date: 2023/11/6 5:31 下午
 */
@RestController
public class VillageController {

    @GetMapping("/village")
    public Mono<String> village(){
        return Mono.just("Hello, village");
    }

    @GetMapping("/demo")
    public String demo() {
        return "Hello, demo";
    }

}
