package com.jobplus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 发布 / 更新职位请求 */
@Data
public class JobPublishRequest {
    private Long id; // 有值则为更新
    @NotNull(message = "企业ID不能为空")
    private Long companyId;
    private String title;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryType; // monthly | daily | hourly
    private String location;
    private String workType;  // remote | onsite | hybrid
    private String requirements;
    private String description;
    private String tags;
}
