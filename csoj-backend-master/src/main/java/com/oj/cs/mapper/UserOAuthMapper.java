package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.UserOAuth;

/** 第三方账号绑定 Mapper */
@Mapper
public interface UserOAuthMapper extends BaseMapper<UserOAuth> {}
