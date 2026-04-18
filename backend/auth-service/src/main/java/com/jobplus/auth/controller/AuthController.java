package com.jobplus.auth.controller;

import com.jobplus.auth.service.AuthService;
import com.jobplus.common.dto.*;
import com.jobplus.common.entity.User;
import com.jobplus.common.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return Map.of("code", 200, "data", resp);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse resp = authService.register(req);
        return Map.of("code", 200, "data", resp);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);
        authService.logout(userId);
        return Map.of("code", 200, "message", "已退出登录");
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        User user = authService.getCurrentUser(token);
        return Map.of("code", 200, "data",
                Map.of("id", user.getId(),
                        "username", user.getUsername(),
                        "realName", user.getRealName(),
                        "role", user.getRole(),
                        "email", user.getEmail(),
                        "phone", user.getPhone()
                ));
    }
}
