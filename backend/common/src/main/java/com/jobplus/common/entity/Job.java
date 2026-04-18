package com.jobplus.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 职位实体
 * 对应表 t_job
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@TableName("t_job")
public class Job {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long companyId;
    private Long hrUserId;
    private String title;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryType;   // monthly | daily | hourly
    private String location;
    private String workType;    // remote | onsite | hybrid
    private String requirements;
    private String description;
    private String tags;        // 逗号分隔
    private Integer status;     // 1=招聘中 0=已关闭
    private Integer viewCount;
    private Integer applyCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
