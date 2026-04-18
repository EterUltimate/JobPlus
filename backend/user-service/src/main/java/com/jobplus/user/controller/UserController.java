package com.jobplus.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jobplus.common.entity.User;
import com.jobplus.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /** 查询用户列表（Admin） */
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role) {
        IPage<User> p = userService.pageUsers(page, size, role);
        return Map.of("code", 200, "data", Map.of(
                "records", p.getRecords(),
                "total", p.getTotal(),
                "pages", p.getPages()
        ));
    }

    /** 查指定用户 */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return Map.of("code", 200, "data", userService.getUser(id));
    }

    /** 更新个人信息 */
    @PutMapping("/me")
    public Map<String, Object> updateMe(
            @RequestBody Map<String, Object> fields,
            HttpServletRequest request) {
        userService.updateUser((Long) request.getAttribute("userId"), fields);
        return Map.of("code", 200, "message", "更新成功");
    }
}
