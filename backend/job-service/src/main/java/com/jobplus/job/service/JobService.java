package com.jobplus.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jobplus.common.constant.RedisKeys;
import com.jobplus.common.dto.*;
import com.jobplus.common.entity.Delivery;
import com.jobplus.common.entity.Job;
import com.jobplus.common.exception.BizException;
import com.jobplus.job.repository.DeliveryMapper;
import com.jobplus.job.repository.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * IJobService 实现 — 职位发布 / 搜索 / 状态更新
 * 对应指导书 4.1.2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobMapper jobMapper;
    private final DeliveryMapper deliveryMapper;
    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;
    private final StringRedisTemplate redisTemplate;

    /** 发布或更新职位（HR） */
    @Transactional
    public Job publishJob(JobPublishRequest req, Long hrUserId) {
        Job job;
        if (req.getId() != null) {
            job = jobMapper.selectById(req.getId());
            if (job == null) throw new BizException("职位不存在");
            if (!job.getHrUserId().equals(hrUserId)) throw new BizException("无权修改此职位");
        } else {
            job = Job.builder().build();
        }

        job.setCompanyId(req.getCompanyId());
        job.setHrUserId(hrUserId);
        job.setTitle(req.getTitle());
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setSalaryType(req.getSalaryType() != null ? req.getSalaryType() : "monthly");
        job.setLocation(req.getLocation());
        job.setWorkType(req.getWorkType() != null ? req.getWorkType() : "onsite");
        job.setRequirements(req.getRequirements());
        job.setDescription(req.getDescription());
        job.setTags(req.getTags());
        job.setStatus(1);
        job.setViewCount(job.getViewCount() != null ? job.getViewCount() : 0);
        job.setApplyCount(job.getApplyCount() != null ? job.getApplyCount() : 0);

        if (req.getId() != null) {
            jobMapper.updateById(job);
        } else {
            jobMapper.insert(job);
        }
        return job;
    }

    /** 搜索职位（分页） */
    public IPage<Job> searchJobs(JobSearchRequest req) {
        LambdaQueryWrapper<Job> q = new LambdaQueryWrapper<Job>()
                .eq(req.getStatus() != null, Job::getStatus, req.getStatus())
                .eq(req.getWorkType() != null, Job::getWorkType, req.getWorkType())
                .ge(req.getSalaryMin() != null, Job::getSalaryMin, req.getSalaryMin())
                .le(req.getSalaryMax() != null, Job::getSalaryMax, req.getSalaryMax())
                .like(req.getLocation() != null, Job::getLocation, req.getLocation())
                .and(req.getKeyword() != null,
                        w -> w.like(Job::getTitle, req.getKeyword())
                           .or().like(Job::getDescription, req.getKeyword())
                )
                .orderByDesc(Job::getCreateTime);

        return jobMapper.selectPage(
                new Page<>(req.getPage(), req.getSize()), q
        );
    }

    /** 职位详情（浏览计数） */
    public Job getJobDetail(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) throw new BizException("职位不存在");
        // 浏览计数（Redis 批量写回 MySQL）
        String viewsKey = RedisKeys.jobViewCount(jobId);
        Long views = redisTemplate.opsForValue().increment(viewsKey);
        if (views != null && views == 1) {
            redisTemplate.expire(viewsKey, 1, TimeUnit.HOURS);
            // 异步更新（实际生产可用 XXL-Job 定时同步）
            job.setViewCount((job.getViewCount() == null ? 0 : job.getViewCount()) + 1);
        } else if (views != null) {
            redisTemplate.opsForValue().increment(viewsKey);
            job.setViewCount((job.getViewCount() == null ? 0 : job.getViewCount()) + views.intValue());
        }
        return job;
    }

    /** 更新职位状态（HR） */
    @Transactional
    public void updateStatus(Long jobId, Integer status, Long hrUserId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) throw new BizException("职位不存在");
        if (!job.getHrUserId().equals(hrUserId)) throw new BizException("无权操作此职位");
        job.setStatus(status);
        jobMapper.updateById(job);
    }

    /** HR 查询收到的所有投递 */
    public List<Map<String, Object>> getHrDeliveries(Long hrUserId) {
        // 先找 HR 发布的所有职位
        List<Job> jobs = jobMapper.selectList(
                new LambdaQueryWrapper<Job>().eq(Job::getHrUserId, hrUserId)
        );
        if (jobs.isEmpty()) return List.of();

        List<Long> jobIds = jobs.stream().map(Job::getId).toList();

        return deliveryMapper.selectList(
                new LambdaQueryWrapper<Delivery>()
                        .in(Delivery::getJobId, jobIds)
                        .orderByDesc(Delivery::getApplyTime)
        ).stream().map(d -> {
            Job j = jobMapper.selectById(d.getJobId());
            Map<String, Object> m = new HashMap<>();
            m.put("delivery", d);
            m.put("jobTitle", j != null ? j.getTitle() : "");
            return m;
        }).toList();
    }
}
