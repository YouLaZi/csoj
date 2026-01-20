package com.oj.cs.model.dto.assignment;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/** 批改作业请求 */
@Data
public class GradeRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 提交ID */
  private Long submitId;

  /** 各题目得分（key为题目ID，value为得分） */
  private Map<Long, Integer> scores;

  /** 总得分 */
  private Integer totalScore;

  /** 批改评语 */
  private String comment;
}
