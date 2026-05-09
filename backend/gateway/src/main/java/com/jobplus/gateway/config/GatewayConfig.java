package com.jobplus.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 路由配置 — 含熔断降级
 * 将 /api/auth/** → auth-service
 *       /api/users/** → user-service
 *       /api/jobs/**  → job-service
 *       /api/resumes/** → job-service（简历读写合并到 job-service 简化）
 *       /api/deliveries/** → job-service
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("authService")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://auth-service"))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("userService")
                                        .setFallbackUri("forward:/fallback/user")))
                        .uri("lb://user-service"))
                .route("job-service", r -> r
                        .path("/api/jobs/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("jobService")
                                        .setFallbackUri("forward:/fallback/job")))
                        .uri("lb://job-service"))
                .route("delivery-service", r -> r
                        .path("/api/deliveries/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("jobService")
                                        .setFallbackUri("forward:/fallback/delivery")))
                        .uri("lb://job-service"))
                .route("resume-service", r -> r
                        .path("/api/resumes/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("jobService")
                                        .setFallbackUri("forward:/fallback/resume")))
                        .uri("lb://job-service"))
                .build();
    }
}
