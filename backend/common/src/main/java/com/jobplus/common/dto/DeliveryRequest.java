package com.jobplus.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 投递简历请求 */
@Data
public class DeliveryRequest {
    @NotNull(message = "职位ID不能为空")
    private Long jobId;
    private Long resumeId; // 可选，默认使用当前用户主简历
}
