package com.jobplus.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobplus.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}
