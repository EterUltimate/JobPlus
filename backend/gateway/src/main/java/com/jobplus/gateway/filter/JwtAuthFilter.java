package com.jobplus.gateway.filter;

import com.jobplus.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway JWT 全局过滤器
 * 1. 从 Authorization: Bearer <token> 提取 JWT
 * 2. 验证签名
 * 3. 将 userId / role 以请求头转发给下游微服务
 * 4. 无 Token 时：公开路径放行，需要认证的路径返回 401
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String[] PUBLIC_EXACT_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/jobs",
            "/actuator/health"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();

        // 放行公开路径
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(req);
        if (token == null) {
            return unauthorized(exchange, "缺少认证 Token");
        }

        if (!jwtUtil.validate(token)) {
            return unauthorized(exchange, "Token 无效或已过期");
        }

        // 检查 Redis 中 Token 是否已登出
        Long userId = jwtUtil.getUserId(token);
        String cached = redisTemplate.opsForValue()
                .get(com.jobplus.common.constant.RedisKeys.token(userId));
        if (cached == null) {
            return unauthorized(exchange, "Token 已失效，请重新登录");
        }

        String role = jwtUtil.getRole(token);

        // 注入下游服务需要的请求头
        ServerHttpRequest mutated = req.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Role", role)
                .header("X-Token", token)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() { return -100; } // 最高优先级

    private boolean isPublic(String path) {
        for (String p : PUBLIC_EXACT_PATHS) {
            if (path.equals(p)) {
                return true;
            }
        }

        if (path.startsWith("/api/jobs/")) {
            String suffix = path.substring("/api/jobs/".length());
            return !suffix.isEmpty() && !suffix.contains("/");
        }

        return false;
    }

    private String extractToken(ServerHttpRequest req) {
        String auth = req.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().put("Content-Type", List.of("application/json"));
        String body = String.format("{\"code\":401,\"message\":\"%s\"}", msg);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
        );
    }
}
