package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 异常告警实体 */
@Data
@TableName("alert")
public class Alert implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 告警标题 */
  private String title;

  /** 告警类型 (error, warning, info) */
  private String alertType;

  /** 告警级别 (critical, high, medium, low) */
  private String severity;

  /** 告警来源 */
  private String source;

  /** 告警内容 */
  private String content;

  /** 异常堆栈信息 */
  private String stackTrace;

  /** 相关请求路径 */
  private String requestPath;

  /** 相关用户ID */
  private Long userId;

  /** 是否已处理 */
  private Integer isProcessed;

  /** 处理人ID */
  private Long processedBy;

  /** 处理时间 */
  private Date processedTime;

  /** 处理备注 */
  private String processNote;

  /** 是否已发送通知 */
  private Integer isNotified;

  /** 通知方式 */
  private String notifyMethod;

  /** 通知次数 */
  private Integer notifyCount;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;
}
