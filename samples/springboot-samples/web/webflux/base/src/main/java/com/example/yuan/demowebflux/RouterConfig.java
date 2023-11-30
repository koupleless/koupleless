package com.example.yuan.demowebflux;

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
    private CityHandler cityHandler;

    @Autowired
    private CountryHandler countryHandler;

    @Bean
    public RouterFunction<ServerResponse> routeCity() {
        return RouterFunctions
                .route(RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), cityHandler::helloCity)
                .andRoute(RequestPredicates.GET("/country").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), countryHandler::helloCountry);
    }

}
