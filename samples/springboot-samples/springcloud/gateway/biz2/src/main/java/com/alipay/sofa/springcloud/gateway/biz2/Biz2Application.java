package com.alipay.sofa.springcloud.gateway.biz2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
public class Biz2Application {

    private final static String WEB_CONTEXT_NAME = "biz2";

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        //@formatter:off
        RouteLocator locator = builder.routes()
                .route("websocket_route", r -> r.path(String.format("/%s/echo", WEB_CONTEXT_NAME))
                        .uri("ws://localhost:9002"))
                .build();

        return locator;
        //@formatter:on
    }

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http.httpBasic().and().csrf().disable().authorizeExchange()
                .pathMatchers("/anything/**").authenticated().anyExchange().permitAll().and()
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService reactiveUserDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password")
                .roles("USER").build();
        return new MapReactiveUserDetailsService(user);
    }

    public static void main(String[] args) {
        SpringApplication.run(Biz2Application.class, args);
    }

}
