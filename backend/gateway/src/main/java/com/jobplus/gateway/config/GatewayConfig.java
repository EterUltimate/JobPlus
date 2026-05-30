package com.jobplus.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;

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

    @Value("${jobplus.gateway.auth-uri:lb://auth-service}")
    private String authServiceUri;

    @Value("${jobplus.gateway.user-uri:lb://user-service}")
    private String userServiceUri;

    @Value("${jobplus.gateway.job-uri:lb://job-service}")
    private String jobServiceUri;

    @Value("${jobplus.gateway.resume-uri:lb://job-service}")
    private String resumeServiceUri;

    @Value("${jobplus.gateway.circuit-breaker.enabled:true}")
    private boolean circuitBreakerEnabled;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> withCircuitBreaker(f, "authService", "forward:/fallback/auth"))
                        .uri(authServiceUri))
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> withCircuitBreaker(f, "userService", "forward:/fallback/user"))
                        .uri(userServiceUri))
                .route("job-service", r -> r
                        .path("/api/jobs/**")
                        .filters(f -> withCircuitBreaker(f, "jobService", "forward:/fallback/job"))
                        .uri(jobServiceUri))
                .route("delivery-service", r -> r
                        .path("/api/deliveries/**")
                        .filters(f -> withCircuitBreaker(f, "jobService", "forward:/fallback/delivery"))
                        .uri(jobServiceUri))
                .route("resume-service", r -> r
                        .path("/api/resumes/**")
                        .filters(f -> withCircuitBreaker(f, "jobService", "forward:/fallback/resume"))
                        .uri(resumeServiceUri))
                .build();
    }

    private GatewayFilterSpec withCircuitBreaker(GatewayFilterSpec filters, String name, String fallbackUri) {
        if (!circuitBreakerEnabled) {
            return filters;
        }
        return filters.circuitBreaker(c -> c
                .setName(name)
                .setFallbackUri(fallbackUri));
    }
}
