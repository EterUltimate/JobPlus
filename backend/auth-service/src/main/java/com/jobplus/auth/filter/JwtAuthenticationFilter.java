package com.jobplus.auth.filter;

import com.jobplus.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器
 * 从 Authorization: Bearer <token> 提取并验证 JWT，
 * 将认证信息注入 SecurityContext。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${jobplus.auth.redis-required:true}")
    private boolean redisRequired;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtUtil.validate(token)) {
            // 检查 Redis 中 Token 是否已失效（登出）
            Long userId = jwtUtil.getUserId(token);
            if (isTokenActive(userId)) {
                String role = jwtUtil.getRole(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                // 将 userId / role 写入请求属性，供 Controller 直接取用
                request.setAttribute("userId", userId);
                request.setAttribute("role", role);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isTokenActive(Long userId) {
        try {
            String cached = redisTemplate.opsForValue()
                    .get(com.jobplus.common.constant.RedisKeys.token(userId));
            return cached != null || !redisRequired;
        } catch (Exception ex) {
            if (redisRequired) {
                log.warn("Redis token validation failed for user {}: {}", userId, ex.getMessage());
                return false;
            }
            log.debug("Redis unavailable, trusting valid JWT for user {}", userId);
            return true;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
