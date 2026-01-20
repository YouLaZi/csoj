package com.oj.cs.model.dto.assignment;

import java.io.Serializable;
import java.util.Date;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 作业查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AssignmentQueryRequest extends PageRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 作业标题（模糊查询） */
  private String title;

  /** 作业类型 */
  private String type;

  /** 难度等级 */
  private String difficulty;

  /** 状态 */
  private String status;

  /** 班级ID */
  private String classId;

  /** 创建用户ID */
  private Long userId;

  /** 截止时间开始 */
  private Date deadlineStart;

  /** 截止时间结束 */
  private Date deadlineEnd;

  /** 排序字段 */
  private String sortField;

  /** 排序方式（asc/desc） */
  private String sortOrder;
}
