package com.jobplus.job.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobplus.common.constant.RedisKeys;
import com.jobplus.common.dto.*;
import com.jobplus.common.entity.Delivery;
import com.jobplus.common.entity.Job;
import com.jobplus.common.entity.Resume;
import com.jobplus.common.exception.BizException;
import com.jobplus.job.repository.DeliveryMapper;
import com.jobplus.job.repository.JobMapper;
import com.jobplus.job.repository.ResumeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IDeliveryService 实现 — 投递申请 / 流程处理
 * 对应指导书 4.1.2 + Sequence.plantuml 投递流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;

    /**
     * 投递简历（核心业务流程）
     * 1. 验证职位状态
     * 2. 检查重复投递
     * 3. 写入 MySQL 投递记录
     * 4. 更新职位投递计数
     * 5. 发送 Kafka 异步消息 → 通知服务 + 推荐服务
     */
    @Transactional
    public Delivery applyJob(DeliveryRequest req, Long userId) {
        // 1. 验证职位有效性
        Job job = jobMapper.selectById(req.getJobId());
        if (job == null) throw new BizException("职位不存在");
        if (job.getStatus() == 0) throw new BizException("该职位已停止招聘");

        // 2. 检查重复投递（同一职位同一用户只能投递一次）
        if (deliveryMapper.selectCount(
                new LambdaQueryWrapper<Delivery>()
                        .eq(Delivery::getJobId, req.getJobId())
                        .eq(Delivery::getUserId, userId)
                        .ne(Delivery::getStatus, Delivery.S_WITHDRAWN)
        ) > 0) {
            throw new BizException("您已投递过该职位，请勿重复投递");
        }

        // 3. 确定简历
        Long resumeId = req.getResumeId();
        if (resumeId == null) {
            Resume resume = resumeMapper.selectOne(
                    new LambdaQueryWrapper<Resume>()
                            .eq(Resume::getUserId, userId)
                            .eq(Resume::getVisibility, 1)
                            .last("LIMIT 1")
            );
            if (resume == null) throw new BizException("未找到可用的公开简历，请先完善简历");
            resumeId = resume.getId();
        }

        // 4. 写入投递记录
        Delivery delivery = Delivery.builder()
                .jobId(req.getJobId())
                .userId(userId)
                .resumeId(resumeId)
                .status(Delivery.S_APPLIED)
                .applyTime(LocalDateTime.now())
                .build();
        deliveryMapper.insert(delivery);

        // 5. 更新职位投递计数（原子 +1）
        job.setApplyCount(job.getApplyCount() + 1);
        jobMapper.updateById(job);

        // 6. Kafka async message (optional)
        Map<String, Object> msg = Map.of(
                "type", "JOB_APPLY",
                "deliveryId", delivery.getId(),
                "jobId", job.getId(),
                "jobTitle", job.getTitle(),
                "userId", userId,
                "hrUserId", job.getHrUserId(),
                "applyTime", delivery.getApplyTime().toString()
        );
        KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate != null) {
            kafkaTemplate.send(RedisKeys.TOPIC_JOB_APPLY, JSON.toJSONString(msg))
                    .whenComplete((r, ex) -> {
                        if (ex != null) log.error("Kafka send failed", ex);
                        else log.info("Kafka sent: deliveryId={}", delivery.getId());
                    });
        } else {
            log.debug("Kafka not configured, skip message");
        }

        log.info("User {} applied job {}, deliveryId={}", userId, req.getJobId(), delivery.getId());
        return delivery;
    }

    /** 处理投递状态（HR） */
    @Transactional
    public Delivery processApplication(Long deliveryId, DeliveryProcessRequest req, Long hrUserId) {
        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery == null) throw new BizException("投递记录不存在");

        // 验证 HR 权限（必须是该职位的发布者）
        Job job = jobMapper.selectById(delivery.getJobId());
        if (!job.getHrUserId().equals(hrUserId)) throw new BizException("无权操作此投递");

        // 合法状态流转
        int s = req.getStatus();
        if (s != Delivery.S_REVIEWING && s != Delivery.S_TESTING
                && s != Delivery.S_HIRED && s != Delivery.S_REJECTED) {
            throw new BizException("无效的状态值");
        }

        delivery.setStatus(s);
        delivery.setFeedback(req.getFeedback());
        delivery.setUpdateTime(LocalDateTime.now());
        deliveryMapper.updateById(delivery);

        // Kafka notification (optional)
        Map<String, Object> msg = Map.of(
                "type", "DELIVERY_STATUS_CHANGE",
                "deliveryId", deliveryId,
                "userId", delivery.getUserId(),
                "status", s,
                "feedback", req.getFeedback() != null ? req.getFeedback() : ""
        );
        KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate != null) {
            kafkaTemplate.send(RedisKeys.TOPIC_JOB_APPLY, JSON.toJSONString(msg));
        } else {
            log.debug("Kafka not configured, skip notification");
        }

        return delivery;
    }

    /** 求职者查询自己的投递列表 */
    public IPage<Map<String, Object>> getMyDeliveries(Long userId, int page, int size) {
        IPage<Delivery> p = deliveryMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Delivery>()
                        .eq(Delivery::getUserId, userId)
                        .orderByDesc(Delivery::getApplyTime)
        );

        List<Map<String, Object>> records = p.getRecords().stream().map(d -> {
            Job job = jobMapper.selectById(d.getJobId());
            return Map.<String, Object>of(
                    "delivery", d,
                    "jobTitle", job != null ? job.getTitle() : "",
                    "companyId", job != null ? job.getCompanyId() : null,
                    "location", job != null ? job.getLocation() : "",
                    "salaryRange", job != null ? formatSalary(job) : ""
            );
        }).toList();

        IPage<Map<String, Object>> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(records);
        return result;
    }

    private String formatSalary(Job job) {
        if (job.getSalaryMin() == null && job.getSalaryMax() == null) return "面议";
        if (job.getSalaryMax() == null) return job.getSalaryMin() + "元以下/月";
        if (job.getSalaryMin() == null) return job.getSalaryMax() + "元以上/月";
        return job.getSalaryMin() + "-" + job.getSalaryMax() + "元/月";
    }
}
