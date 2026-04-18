package com.jobplus.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobplus.common.entity.User;
import com.jobplus.user.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    /** 分页查询用户 */
    public IPage<User> pageUsers(int page, int size, String role) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if (role != null) q.eq(User::getRole, role);
        q.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(new Page<>(page, size), q);
    }

    /** 根据 userId 查用户 */
    public User getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    /** 更新用户信息 */
    public void updateUser(Long userId, Map<String, Object> fields) {
        User u = userMapper.selectById(userId);
        if (u == null) return;
        fields.forEach((k, v) -> {
            switch (k) {
                case "realName" -> u.setRealName((String) v);
                case "phone"    -> u.setPhone((String) v);
                case "email"    -> u.setEmail((String) v);
                case "avatar"   -> u.setAvatar((String) v);
            }
        });
        userMapper.updateById(u);
    }
}
