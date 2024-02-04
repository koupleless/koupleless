package com.example.yuan.bizwebflux;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author: yuanyuan
 * @date: 2023/11/6 7:53 下午
 */
@Component
public class BizHandler {

    public Mono<ServerResponse> helloBiz(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Hello, biz webflux!"));
    }

}
