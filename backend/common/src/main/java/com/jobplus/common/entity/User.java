package com.jobplus.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 用户实体（求职者 / HR / 管理员）
 * 对应表 t_user
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;
    private String email;
    /** SEEKER | HR | ADMIN */
    private String role;
    private Long companyId;
    private String avatar;
    private Integer status; // 1=正常 0=禁用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
