package com.jobplus.job.controller;

import com.jobplus.common.dto.ResumeUpdateRequest;
import com.jobplus.common.entity.Resume;
import com.jobplus.job.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 简历管理（读写分离给 resume-service，这里提供读写入口） */
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /** 获取我的简历 */
    @GetMapping("/me")
    public Map<String, Object> getMyResume(HttpServletRequest request) {
        Resume resume = resumeService.getByUserId((Long) request.getAttribute("userId"));
        return Map.of("code", 200, "data", resume != null ? resume : Map.of());
    }

    /** 更新我的简历 */
    @PutMapping("/me")
    public Map<String, Object> updateMyResume(
            @Valid @RequestBody ResumeUpdateRequest req,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"SEEKER".equals(role) && !"ADMIN".equals(role))
            return Map.of("code", 403, "message", "仅求职者可编辑简历");

        Resume resume = resumeService.upsert((Long) request.getAttribute("userId"), req);
        return Map.of("code", 200, "data", resume, "message", "保存成功");
    }
}
