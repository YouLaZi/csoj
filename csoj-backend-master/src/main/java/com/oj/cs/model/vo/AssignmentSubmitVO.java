package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;

/** 作业提交视图对象 */
@Data
public class AssignmentSubmitVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 提交ID */
  private Long id;

  /** 作业ID */
  private Long assignmentId;

  /** 作业标题 */
  private String assignmentTitle;

  /** 学生ID */
  private Long userId;

  /** 学生姓名 */
  private String userName;

  /** 学生账号 */
  private String userAccount;

  /** 提交的题目ID列表 */
  private List<Long> questionIds;

  /** 各题目得分 */
  private Map<Long, Integer> scores;

  /** 总得分 */
  private Integer totalScore;

  /** 提交状态（SUBMITTED-已提交，GRADED-已批改） */
  private String status;

  /** 批改评语 */
  private String comment;

  /** 批改教师ID */
  private Long gradedBy;

  /** 批改教师姓名 */
  private String gradedByName;

  /** 批改时间 */
  private Date gradedTime;

  /** 提交时间 */
  private Date submitTime;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;
}
