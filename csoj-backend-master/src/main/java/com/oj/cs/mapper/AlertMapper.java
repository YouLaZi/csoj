package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.Alert;

/** 异常告警 Mapper */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {}
