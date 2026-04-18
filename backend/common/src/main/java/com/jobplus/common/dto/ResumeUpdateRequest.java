package com.jobplus.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 更新 / 创建简历请求 */
@Data
public class ResumeUpdateRequest {
    private String realName;
    private String gender;
    private Integer age;
    private String education;
    private String major;
    private String phone;
    private String email;
    private Integer workExp;
    private String skills;
    private String contentJson;
    private String fileUrl;
    private Integer visibility = 1;
}
