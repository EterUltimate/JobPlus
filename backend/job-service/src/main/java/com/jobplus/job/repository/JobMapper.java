package com.jobplus.job.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobplus.common.entity.Job;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobMapper extends BaseMapper<Job> {}
