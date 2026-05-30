package com.jobplus.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobplus.auth.repository.UserMapper;
import com.jobplus.common.dto.*;
import com.jobplus.common.entity.User;
import com.jobplus.common.exception.BizException;
import com.jobplus.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 登录 */
    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        );
        if (user == null) throw new BizException("用户不存在");
        if (user.getStatus() == 0) throw new BizException("账号已被禁用");
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException("密码错误");
        }

        String token = jwtUtil.sign(user.getId(), user.getUsername(), user.getRole());
        cacheToken(user.getId(), token);
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    /** 注册 */
    public LoginResponse register(RegisterRequest req) {
        // 查重
        if (userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        ) > 0) {
            throw new BizException("用户名已存在");
        }

        User user = User.builder()
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .realName(req.getRealName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .role(req.getRole() != null ? req.getRole() : "SEEKER")
                .status(1)
                .build();
        userMapper.insert(user);

        String token = jwtUtil.sign(user.getId(), user.getUsername(), user.getRole());
        cacheToken(user.getId(), token);
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    /** 注销 Token（登出） */
    public void logout(Long userId) {
        try {
            redisTemplate.delete(com.jobplus.common.constant.RedisKeys.token(userId));
        } catch (Exception ex) {
            log.warn("Redis token deletion skipped for user {}: {}", userId, ex.getMessage());
        }
    }

    /** 解析当前登录用户信息 */
    public User getCurrentUser(String token) {
        Long userId = jwtUtil.getUserId(token);
        return userMapper.selectById(userId);
    }

    private void cacheToken(Long userId, String token) {
        try {
            redisTemplate.opsForValue().set(
                    com.jobplus.common.constant.RedisKeys.token(userId),
                    token, 24, TimeUnit.HOURS
            );
        } catch (Exception ex) {
            log.warn("Redis token cache skipped for user {}: {}", userId, ex.getMessage());
        }
    }
}
