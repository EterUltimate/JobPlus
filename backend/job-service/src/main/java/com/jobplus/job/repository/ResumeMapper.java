package com.jobplus.job.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobplus.common.entity.Resume;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {}
