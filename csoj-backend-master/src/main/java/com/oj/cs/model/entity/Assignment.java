package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 作业 */
@TableName(value = "assignment")
@Data
public class Assignment implements Serializable {

  /** id */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 作业标题 */
  private String title;

  /** 作业描述 */
  private String description;

  /** 作业类型（PRACTICE-练习题，EXAM-考试题） */
  private String type;

  /** 关联的题目ID列表（JSON数组格式） */
  private String questionIds;

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

  /** 班级ID（用于区分不同班级） */
  private String classId;

  /** 是否公开（true-所有学生可见，false-指定班级可见） */
  private Boolean isPublic;

  /** 状态（DRAFT-草稿，PUBLISHED-已发布，CLOSED-已关闭） */
  private String status;

  /** 提交数量 */
  private Integer submitCount;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
