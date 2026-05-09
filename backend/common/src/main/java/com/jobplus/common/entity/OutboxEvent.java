package com.jobplus.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Outbox 事件实体
 * 保证异步事件可靠投递
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_outbox_event")
public class OutboxEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventType;
    private String aggregateType;
    private Long aggregateId;
    private String topic;
    private String payload;
    private String status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
