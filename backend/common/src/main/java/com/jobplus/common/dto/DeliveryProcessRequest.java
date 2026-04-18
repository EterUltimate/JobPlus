package com.jobplus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** HR 处理投递请求 */
@Data
public class DeliveryProcessRequest {
    @NotNull(message = "状态不能为空")
    private Integer status; // 2=待查看 3=笔试中 4=录用 5=拒绝
    private String feedback;
}
