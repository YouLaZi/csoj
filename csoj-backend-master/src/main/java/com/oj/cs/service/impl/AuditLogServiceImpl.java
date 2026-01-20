package com.oj.cs.service.impl;

import java.util.Date;

import jakarta.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.mapper.AuditLogMapper;
import com.oj.cs.model.dto.auditlog.AuditLogQueryRequest;
import com.oj.cs.model.entity.AuditLog;
import com.oj.cs.service.AuditLogService;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/** 操作审计日志服务实现 */
@Service
@Slf4j
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog>
    implements AuditLogService {

  @Resource private AuditLogService auditLogService;

  /**
   * 异步保存审计日志
   *
   * @param auditLog 审计日志
   */
  @Async
  @Override
  public void saveAsync(AuditLog auditLog) {
    try {
      boolean saved = auditLogService.save(auditLog);
      if (!saved) {
        log.error("审计日志保存失败: {}", auditLog);
      }
    } catch (Exception e) {
      log.error("审计日志保存异常", e);
    }
  }

  /**
   * 分页查询审计日志
   *
   * @param current 当前页
   * @param size 每页大小
   * @param queryRequest 查询条件
   * @return 分页结果
   */
  @Override
  public Page<AuditLog> queryAuditLogs(long current, long size, AuditLogQueryRequest queryRequest) {
    QueryWrapper<AuditLog> queryWrapper = getQueryWrapper(queryRequest);

    // 处理排序
    String sortField = queryRequest.getSortField();
    String sortOrder = queryRequest.getSortOrder();

    if (StrUtil.isNotBlank(sortField)) {
      if ("desc".equalsIgnoreCase(sortOrder)) {
        queryWrapper.orderByDesc(sortField);
      } else {
        queryWrapper.orderByAsc(sortField);
      }
    } else {
      // 默认按创建时间倒序
      queryWrapper.orderByDesc("createTime");
    }

    return auditLogService.page(new Page<>(current, size), queryWrapper);
  }

  /**
   * 构建查询条件
   *
   * @param queryRequest 查询条件
   * @return QueryWrapper
   */
  @Override
  public QueryWrapper<AuditLog> getQueryWrapper(AuditLogQueryRequest queryRequest) {
    QueryWrapper<AuditLog> queryWrapper = new QueryWrapper<>();

    if (queryRequest == null) {
      return queryWrapper;
    }

    // 用户ID精确查询
    Long userId = queryRequest.getUserId();
    if (userId != null) {
      queryWrapper.eq("user_id", userId);
    }

    // 用户账号模糊查询
    String userAccount = queryRequest.getUserAccount();
    if (StrUtil.isNotBlank(userAccount)) {
      queryWrapper.like("user_account", userAccount);
    }

    // 用户姓名模糊查询
    String userName = queryRequest.getUserName();
    if (StrUtil.isNotBlank(userName)) {
      queryWrapper.like("user_name", userName);
    }

    // 操作模块精确查询
    String module = queryRequest.getModule();
    if (StrUtil.isNotBlank(module)) {
      queryWrapper.eq("module", module);
    }

    // 操作类型精确查询
    String operationType = queryRequest.getOperationType();
    if (StrUtil.isNotBlank(operationType)) {
      queryWrapper.eq("operation_type", operationType);
    }

    // 操作状态精确查询
    String status = queryRequest.getStatus();
    if (StrUtil.isNotBlank(status)) {
      queryWrapper.eq("status", status);
    }

    // 客户端IP精确查询
    String clientIp = queryRequest.getClientIp();
    if (StrUtil.isNotBlank(clientIp)) {
      queryWrapper.eq("client_ip", clientIp);
    }

    // 时间范围查询
    Date startTime = queryRequest.getStartTime();
    Date endTime = queryRequest.getEndTime();
    if (startTime != null) {
      queryWrapper.ge("create_time", startTime);
    }
    if (endTime != null) {
      queryWrapper.le("create_time", endTime);
    }

    return queryWrapper;
  }
}
