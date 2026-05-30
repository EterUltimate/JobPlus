package com.jobplus.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Gateway 熔断降级 Fallback 控制器
 * 当下游服务不可用时返回统一的 503 响应
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/{service}")
    public ResponseEntity<Map<String, Object>> fallback(@PathVariable("service") String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "code", 503,
                "message", service + " service is temporarily unavailable",
                "timestamp", Instant.now().toString(),
                "retryable", true
        ));
    }
}
