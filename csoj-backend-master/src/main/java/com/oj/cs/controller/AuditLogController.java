package com.oj.cs.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.auditlog.AuditLogQueryRequest;
import com.oj.cs.model.entity.AuditLog;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.AuditLogService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 操作审计日志接口 */
@RestController
@RequestMapping("/audit")
@Slf4j
public class AuditLogController {

  @Resource private AuditLogService auditLogService;

  @Resource private UserService userService;

  /**
   * 分页查询审计日志（仅管理员）
   *
   * @param auditLogQueryRequest 查询条件
   * @param request 请求
   * @return 分页结果
   */
  @PostMapping("/list/page")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<AuditLog>> listAuditLogByPage(
      @RequestBody AuditLogQueryRequest auditLogQueryRequest, HttpServletRequest request) {
    if (auditLogQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long current = auditLogQueryRequest.getCurrent();
    long size = auditLogQueryRequest.getPageSize();
    Page<AuditLog> auditLogPage =
        auditLogService.queryAuditLogs(current, size, auditLogQueryRequest);
    return ResultUtils.success(auditLogPage);
  }

  /**
   * 查询当前用户的审计日志
   *
   * @param auditLogQueryRequest 查询条件
   * @param request 请求
   * @return 分页结果
   */
  @PostMapping("/list/my")
  public BaseResponse<Page<AuditLog>> listMyAuditLogByPage(
      @RequestBody AuditLogQueryRequest auditLogQueryRequest, HttpServletRequest request) {
    if (auditLogQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);
    if (loginUser == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    // 设置查询条件为当前用户
    auditLogQueryRequest.setUserId(loginUser.getId());

    long current = auditLogQueryRequest.getCurrent();
    long size = auditLogQueryRequest.getPageSize();

    // 限制每页最大数量
    if (size > 50) {
      size = 50;
    }

    Page<AuditLog> auditLogPage =
        auditLogService.queryAuditLogs(current, size, auditLogQueryRequest);
    return ResultUtils.success(auditLogPage);
  }

  /**
   * 根据ID获取审计日志详情（仅管理员）
   *
   * @param id 日志ID
   * @param request 请求
   * @return 审计日志详情
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<AuditLog> getAuditLogById(@RequestParam long id, HttpServletRequest request) {
    if (id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    AuditLog auditLog = auditLogService.getById(id);
    return ResultUtils.success(auditLog);
  }
}
