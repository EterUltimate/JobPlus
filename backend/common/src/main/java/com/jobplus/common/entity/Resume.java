package com.jobplus.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 简历实体
 * 对应表 t_resume（1:1 User）
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@TableName("t_resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;         // 1:1 关联 User
    private String realName;
    private String gender;       // male | female | other
    private Integer age;
    private String education;
    private String major;
    private String phone;
    private String email;
    private Integer workExp;     // 工作年限
    private String skills;       // 逗号分隔
    private String contentJson;  // JSON格式简历详情
    private String fileUrl;      // 附件URL（MinIO）
    private Integer visibility;  // 1=公开 0=私密
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
