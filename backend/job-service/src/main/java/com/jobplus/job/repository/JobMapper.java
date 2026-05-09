package com.jobplus.job.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobplus.common.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface JobMapper extends BaseMapper<Job> {

    /**
     * 原子递增职位投递计数
     * 避免并发场景下的计数丢失
     */
    @Update("UPDATE t_job SET apply_count = COALESCE(apply_count, 0) + 1 WHERE id = #{jobId}")
    int incrementApplyCount(Long jobId);
}
