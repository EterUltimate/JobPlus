package com.jobplus.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 路由配置
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
                        .uri("lb://auth-service"))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("lb://user-service"))
                .route("job-service", r -> r
                        .path("/api/jobs/**")
                        .uri("lb://job-service"))
                .route("delivery-service", r -> r
                        .path("/api/deliveries/**")
                        .uri("lb://job-service"))
                .route("resume-service", r -> r
                        .path("/api/resumes/**")
                        .uri("lb://job-service"))
                .build();
    }
}
