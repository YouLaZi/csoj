package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 作业视图对象 */
@Data
public class AssignmentVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 作业ID */
  private Long id;

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

  /** 创建用户ID（教师） */
  private Long userId;

  /** 创建用户姓名 */
  private String userName;

  /** 班级ID */
  private String classId;

  /** 是否公开 */
  private Boolean isPublic;

  /** 状态（DRAFT-草稿，PUBLISHED-已发布，CLOSED-已关闭） */
  private String status;

  /** 提交数量 */
  private Integer submitCount;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 当前用户是否已提交 */
  private Boolean hasSubmitted;
}
