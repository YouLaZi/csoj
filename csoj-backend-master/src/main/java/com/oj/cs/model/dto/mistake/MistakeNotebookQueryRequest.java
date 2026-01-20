package com.oj.cs.model.dto.mistake;

import java.io.Serializable;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 错题本查询请求 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MistakeNotebookQueryRequest extends PageRequest implements Serializable {

  /** 用户ID（查询自己的错题，管理员可查所有） */
  private Long userId;

  /** 题目ID */
  private Long questionId;

  /** 错误类型 */
  private String mistakeType;

  /** 错题分类 */
  private String category;

  /** 是否已复习 */
  private Integer isReviewed;

  /** 排序字段 */
  private String sortField;

  /** 排序方向 */
  private String sortOrder;

  private static final long serialVersionUID = 1L;
}
