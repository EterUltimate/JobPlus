package com.jobplus.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jobplus.common.dto.JobPublishRequest;
import com.jobplus.common.dto.JobSearchRequest;
import com.jobplus.common.entity.Job;
import com.jobplus.job.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /** 搜索职位（分页） */
    @GetMapping
    public Map<String, Object> searchJobs(JobSearchRequest req) {
        IPage<Job> page = jobService.searchJobs(req);
        return Map.of(
                "code", 200,
                "data", Map.of(
                        "records", page.getRecords(),
                        "total", page.getTotal(),
                        "pages", page.getPages(),
                        "current", page.getCurrent(),
                        "size", page.getSize()
                )
        );
    }

    /** 职位详情 */
    @GetMapping("/{id}")
    public Map<String, Object> getJobDetail(@PathVariable Long id) {
        return Map.of("code", 200, "data", jobService.getJobDetail(id));
    }

    /** 发布 / 更新职位（HR） */
    @PostMapping
    public Map<String, Object> publishJob(
            @Valid @RequestBody JobPublishRequest req,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"HR".equals(role) && !"ADMIN".equals(role))
            return Map.of("code", 403, "message", "仅HR可发布职位");

        Job job = jobService.publishJob(req, (Long) request.getAttribute("userId"));
        return Map.of("code", 200, "data", job, "message", "发布成功");
    }

    /** 更新职位状态（HR 关闭职位） */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"HR".equals(role) && !"ADMIN".equals(role))
            return Map.of("code", 403, "message", "无权操作");

        jobService.updateStatus(id, status, (Long) request.getAttribute("userId"));
        return Map.of("code", 200, "message", "状态已更新");
    }

    /** HR 查询收到的投递列表 */
    @GetMapping("/hr/deliveries")
    public Map<String, Object> getHrDeliveries(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"HR".equals(role) && !"ADMIN".equals(role))
            return Map.of("code", 403, "message", "无权访问");

        List<Map<String, Object>> list = jobService.getHrDeliveries((Long) request.getAttribute("userId"));
        return Map.of("code", 200, "data", list);
    }
}
