package com.maayn.apigateway;

import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> transactionServiceRoute() {
        return GatewayRouterFunctions.route("transaction-service-route")
                .route(path("/api/transaction_service/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("transaction-service"))
                .build();
    }
}
