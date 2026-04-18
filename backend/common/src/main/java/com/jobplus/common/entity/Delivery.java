package com.jobplus.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 投递记录实体
 * 对应表 t_delivery
 *
 * 状态流转：
 *   1 已投递 → 2 待查看 → 3 笔试中 → 4 入职（录用）
 *                          ↘ 5 已拒绝
 *              ↘ 5 已拒绝（可不经笔试直接拒绝）
 *   可撤回 → 6 已撤回
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@TableName("t_delivery")
public class Delivery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private Long userId;
    private Long resumeId;
    /** 1已投递 2待查看 3笔试中 4入职 5已拒绝 6已撤回 */
    private Integer status;
    private String feedback;     // HR反馈
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public static final int S_APPLIED    = 1; // 已投递
    public static final int S_REVIEWING  = 2; // 待查看
    public static final int S_TESTING    = 3; // 笔试中
    public static final int S_HIRED       = 4; // 入职（录用）
    public static final int S_REJECTED    = 5; // 已拒绝
    public static final int S_WITHDRAWN   = 6; // 已撤回
}
