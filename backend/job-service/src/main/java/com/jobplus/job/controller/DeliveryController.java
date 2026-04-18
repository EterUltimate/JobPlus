package com.jobplus.job.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jobplus.common.dto.DeliveryProcessRequest;
import com.jobplus.common.dto.DeliveryRequest;
import com.jobplus.common.entity.Delivery;
import com.jobplus.job.service.DeliveryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    /** 投递简历（求职者） */
    @PostMapping
    public Map<String, Object> apply(
            @Valid @RequestBody DeliveryRequest req,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"SEEKER".equals(role) && !"ADMIN".equals(role))
            return Map.of("code", 403, "message", "仅求职者可投递简历");

        Delivery delivery = deliveryService.applyJob(req, (Long) request.getAttribute("userId"));
        return Map.of("code", 200, "data", delivery, "message", "投递成功");
    }

    /** 我的投递列表（求职者） */
    @GetMapping("/me")
    public Map<String, Object> myDeliveries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        IPage<Map<String, Object>> p = deliveryService.getMyDeliveries(
                (Long) request.getAttribute("userId"), page, size);
        return Map.of(
                "code", 200,
                "data", Map.of(
                        "records", p.getRecords(),
                        "total", p.getTotal(),
                        "pages", p.getPages(),
                        "current", p.getCurrent()
                )
        );
    }

    /** HR 处理投递（改变状态） */
    @PutMapping("/{id}")
    public Map<String, Object> process(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryProcessRequest req,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"HR".equals(role) && !"ADMIN".equals(role))
            return Map.of("code", 403, "message", "仅HR可处理投递");

        Delivery delivery = deliveryService.processApplication(
                id, req, (Long) request.getAttribute("userId"));
        return Map.of("code", 200, "data", delivery, "message", "处理成功");
    }
}
