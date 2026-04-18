package com.jobplus.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 注册请求 */
@Data
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    private String realName;
    /** SEEKER | HR */
    private String role = "SEEKER";
    private String phone;
    private String email;
}
