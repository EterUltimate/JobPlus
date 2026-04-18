package com.jobplus.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobplus.common.dto.ResumeUpdateRequest;
import com.jobplus.common.entity.Resume;
import com.jobplus.job.repository.ResumeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 简历服务（读写整合入口） */
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeMapper resumeMapper;

    public Resume getByUserId(Long userId) {
        return resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getUserId, userId)
                        .last("LIMIT 1")
        );
    }

    @Transactional
    public Resume upsert(Long userId, ResumeUpdateRequest req) {
        Resume existing = getByUserId(userId);
        if (existing != null) {
            // 更新
            if (req.getRealName() != null) existing.setRealName(req.getRealName());
            if (req.getGender() != null) existing.setGender(req.getGender());
            if (req.getAge() != null) existing.setAge(req.getAge());
            if (req.getEducation() != null) existing.setEducation(req.getEducation());
            if (req.getMajor() != null) existing.setMajor(req.getMajor());
            if (req.getPhone() != null) existing.setPhone(req.getPhone());
            if (req.getEmail() != null) existing.setEmail(req.getEmail());
            if (req.getWorkExp() != null) existing.setWorkExp(req.getWorkExp());
            if (req.getSkills() != null) existing.setSkills(req.getSkills());
            if (req.getContentJson() != null) existing.setContentJson(req.getContentJson());
            if (req.getFileUrl() != null) existing.setFileUrl(req.getFileUrl());
            if (req.getVisibility() != null) existing.setVisibility(req.getVisibility());
            resumeMapper.updateById(existing);
            return existing;
        } else {
            // 新建
            Resume r = Resume.builder()
                    .userId(userId)
                    .realName(req.getRealName())
                    .gender(req.getGender())
                    .age(req.getAge())
                    .education(req.getEducation())
                    .major(req.getMajor())
                    .phone(req.getPhone())
                    .email(req.getEmail())
                    .workExp(req.getWorkExp() != null ? req.getWorkExp() : 0)
                    .skills(req.getSkills())
                    .contentJson(req.getContentJson())
                    .fileUrl(req.getFileUrl())
                    .visibility(req.getVisibility() != null ? req.getVisibility() : 1)
                    .build();
            resumeMapper.insert(r);
            return r;
        }
    }
}
