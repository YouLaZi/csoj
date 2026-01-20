package com.oj.cs.model.dto.mistake;

import java.io.Serializable;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 错题本添加请求 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MistakeNotebookAddRequest extends PageRequest implements Serializable {

  /** 题目ID */
  private Long questionId;

  /** 提交记录ID */
  private Long submitId;

  /** 错误类型 */
  private String mistakeType;

  /** 错误信息 */
  private String errorMessage;

  /** 用户笔记 */
  private String notes;

  /** 错题分类 */
  private String category;

  private static final long serialVersionUID = 1L;
}
