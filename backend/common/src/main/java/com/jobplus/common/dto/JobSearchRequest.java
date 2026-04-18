package com.jobplus.common.dto;

import lombok.Data;

/** 职位搜索请求 */
@Data
public class JobSearchRequest {
    private String keyword;
    private String location;
    private String workType;   // remote | onsite | hybrid
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer status = 1; // 默认只查招聘中
    private Integer page = 1;
    private Integer size = 10;
}
