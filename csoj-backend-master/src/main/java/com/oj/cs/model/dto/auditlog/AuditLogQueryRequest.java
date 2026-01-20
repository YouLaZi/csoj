package com.oj.cs.model.dto.auditlog;

import java.io.Serializable;
import java.util.Date;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 审计日志查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AuditLogQueryRequest extends PageRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 用户ID */
  private Long userId;

  /** 用户账号（模糊查询） */
  private String userAccount;

  /** 用户姓名（模糊查询） */
  private String userName;

  /** 操作模块 */
  private String module;

  /** 操作类型 */
  private String operationType;

  /** 操作状态 */
  private String status;

  /** 客户端IP */
  private String clientIp;

  /** 开始时间 */
  private Date startTime;

  /** 结束时间 */
  private Date endTime;

  /** 排序字段 */
  private String sortField;

  /** 排序方式（asc/desc） */
  private String sortOrder;
}
