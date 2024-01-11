package com.example.yuan.bizwebflux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author: yuanyuan
 * @date: 2023/11/6 5:23 下午
 */
@Configuration
public class RouterConfig {

    @Autowired
    private BizHandler bizHandler;

    @Bean
    public RouterFunction<ServerResponse> routeRequest() {
        return RouterFunctions
                .route(RequestPredicates.GET("/biz").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), bizHandler::helloBiz);
    }

}
