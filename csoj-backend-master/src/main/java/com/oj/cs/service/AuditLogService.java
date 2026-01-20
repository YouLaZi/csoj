package com.oj.cs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.auditlog.AuditLogQueryRequest;
import com.oj.cs.model.entity.AuditLog;

/** 操作审计日志服务 */
public interface AuditLogService extends IService<AuditLog> {

  /**
   * 异步保存审计日志
   *
   * @param auditLog 审计日志
   */
  void saveAsync(AuditLog auditLog);

  /**
   * 分页查询审计日志
   *
   * @param current 当前页
   * @param size 每页大小
   * @param queryRequest 查询条件
   * @return 分页结果
   */
  Page<AuditLog> queryAuditLogs(long current, long size, AuditLogQueryRequest queryRequest);

  /**
   * 构建查询条件
   *
   * @param queryRequest 查询条件
   * @return QueryWrapper
   */
  QueryWrapper<AuditLog> getQueryWrapper(AuditLogQueryRequest queryRequest);
}
