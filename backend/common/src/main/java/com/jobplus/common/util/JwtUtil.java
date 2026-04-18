package com.jobplus.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * 签发格式：{userId, username, role, exp}
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:jobplus-secret-key-must-be-at-least-256-bits-long-for-hs256}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs; // 默认 24h

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 Token */
    public String sign(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of(
                        "username", username,
                        "role", role
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    /** 解析 Token，返回 Claims */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验并返回 userId */
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public String getRole(String token) {
        return (String) parse(token).get("role");
    }

    public String getUsername(String token) {
        return (String) parse(token).get("username");
    }

    /** 校验 Token 是否有效 */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
