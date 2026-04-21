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

    private final PublicKeyProvider publicKeyProvider;

    public GatewayConfig(PublicKeyProvider publicKeyProvider) {
        this.publicKeyProvider = publicKeyProvider;
    }

    // ---------- IAM (auth) — public, no JWT filter ----------
    @Bean
    public RouterFunction<ServerResponse> iamServiceRoute() {
        return GatewayRouterFunctions.route("iam-service-route")
                .route(path("/api/auth/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("iam-service"))
                .build();
    }

    // ---------- Account service ----------
    @Bean
    public RouterFunction<ServerResponse> accountServiceRoute() {
        return GatewayRouterFunctions.route("account-service-route")
                .route(path("/api/accounts/**"), HandlerFunctions.http())
                .route(path("/api/accounts_service/**"), HandlerFunctions.http())
                .route(path("/api/customers/**"), HandlerFunctions.http())
                .filter(JwtAuthFilter.withJwtAuth(publicKeyProvider))
                .filter(LoadBalancerFilterFunctions.lb("account-service"))
                .build();
    }

    // ---------- Card service ----------
    @Bean
    public RouterFunction<ServerResponse> cardServiceRoute() {
        return GatewayRouterFunctions.route("card-service-route")
                .route(path("/api/card/**"), HandlerFunctions.http())
                .filter(JwtAuthFilter.withJwtAuth(publicKeyProvider))
                .filter(LoadBalancerFilterFunctions.lb("card-service"))
                .build();
    }

    // ---------- Financial service ----------
    @Bean
    public RouterFunction<ServerResponse> financialServiceRoute() {
        return GatewayRouterFunctions.route("financial-service-route")
                .route(path("/api/financial_service/**"), HandlerFunctions.http())
                .filter(JwtAuthFilter.withJwtAuth(publicKeyProvider))
                .filter(LoadBalancerFilterFunctions.lb("financial-service"))
                .build();
    }

    // ---------- Notification service ----------
    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return GatewayRouterFunctions.route("notification-service-route")
                .route(path("/api/notification/**"), HandlerFunctions.http())
                .route(path("/api/workflow/**"), HandlerFunctions.http())
                .filter(JwtAuthFilter.withJwtAuth(publicKeyProvider))
                .filter(LoadBalancerFilterFunctions.lb("notification-service"))
                .build();
    }

    // ---------- Audit service ----------
    @Bean
    public RouterFunction<ServerResponse> auditServiceRoute() {
        return GatewayRouterFunctions.route("audit-service-route")
                .route(path("/api/audit/**"), HandlerFunctions.http())
                .filter(JwtAuthFilter.withJwtAuth(publicKeyProvider))
                .filter(LoadBalancerFilterFunctions.lb("audit-service"))
                .build();
    }

    // ---------- Transaction service ----------
    @Bean
    public RouterFunction<ServerResponse> transactionServiceRoute() {
        return GatewayRouterFunctions.route("transaction-service-route")
                .route(path("/api/transaction_service/**"), HandlerFunctions.http())
                .filter(JwtAuthFilter.withJwtAuth(publicKeyProvider))
                .filter(LoadBalancerFilterFunctions.lb("transaction-service"))
                .build();
    }
}
