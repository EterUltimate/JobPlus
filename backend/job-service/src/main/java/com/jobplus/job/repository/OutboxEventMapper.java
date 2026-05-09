package com.jobplus.job.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobplus.common.entity.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * OutboxEvent Mapper
 */
@Mapper
public interface OutboxEventMapper extends BaseMapper<OutboxEvent> {
}
