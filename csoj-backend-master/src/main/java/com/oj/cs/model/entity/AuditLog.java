package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 操作审计日志 */
@TableName(value = "audit_log")
@Data
public class AuditLog implements Serializable {

  /** id */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 操作用户ID */
  private Long userId;

  /** 操作用户账号 */
  private String userAccount;

  /** 操作用户姓名 */
  private String userName;

  /** 操作模块 */
  private String module;

  /** 操作类型（LOGIN/LOGOUT/CREATE/UPDATE/DELETE/QUERY/EXPORT/IMPORT等） */
  private String operationType;

  /** 操作描述 */
  private String description;

  /** 请求方法 */
  private String method;

  /** 请求参数 */
  private String params;

  /** 返回结果 */
  private String result;

  /** 执行时长（毫秒） */
  private Long duration;

  /** 操作状态（SUCCESS/FAILURE） */
  private String status;

  /** 错误信息 */
  private String errorMsg;

  /** 客户端IP */
  private String clientIp;

  /** 用户代理 */
  private String userAgent;

  /** 创建时间 */
  private Date createTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  private static final long serialVersionUID = 1L;
}
