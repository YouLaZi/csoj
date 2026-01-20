package com.oj.cs.model.dto.assignment;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 作业提交请求 */
@Data
public class AssignmentSubmitRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 作业ID */
  private Long assignmentId;

  /** 提交的题目ID列表 */
  private List<Long> questionIds;
}
