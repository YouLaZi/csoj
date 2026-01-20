package com.oj.cs.model.dto.contest;

import java.io.Serializable;
import java.util.Date;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 比赛查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContestQueryRequest extends PageRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 比赛名称（模糊查询） */
  private String title;

  /** 比赛类型 */
  private String type;

  /** 状态 */
  private String status;

  /** 创建用户ID */
  private Long userId;

  /** 开始时间范围-开始 */
  private Date startTimeBegin;

  /** 开始时间范围-结束 */
  private Date startTimeEnd;

  /** 排序字段 */
  private String sortField;

  /** 排序方式（asc/desc） */
  private String sortOrder;
}
