package com.oj.cs.model.dto.assignment;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 创建作业请求 */
@Data
public class AssignmentAddRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 作业标题 */
  private String title;

  /** 作业描述 */
  private String description;

  /** 作业类型（PRACTICE-练习题，EXAM-考试题） */
  private String type;

  /** 关联的题目ID列表 */
  private List<Long> questionIds;

  /** 难度等级（EASY-简单，MEDIUM-中等，HARD-困难） */
  private String difficulty;

  /** 总分 */
  private Integer totalScore;

  /** 及格分数 */
  private Integer passScore;

  /** 截止时间 */
  private Date deadline;

  /** 班级ID */
  private String classId;

  /** 是否公开 */
  private Boolean isPublic;
}
