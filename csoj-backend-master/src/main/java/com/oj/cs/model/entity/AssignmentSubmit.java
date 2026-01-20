package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 作业提交 */
@TableName(value = "assignment_submit")
@Data
public class AssignmentSubmit implements Serializable {

  /** id */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 作业ID */
  private Long assignmentId;

  /** 学生ID */
  private Long userId;

  /** 提交的题目ID列表（JSON数组格式） */
  private String questionIds;

  /** 各题目得分（JSON对象格式，key为题目ID，value为得分） */
  private String scores;

  /** 总得分 */
  private Integer totalScore;

  /** 提交状态（SUBMITTED-已提交，GRADED-已批改） */
  private String status;

  /** 批改评语 */
  private String comment;

  /** 批改教师ID */
  private Long gradedBy;

  /** 批改时间 */
  private Date gradedTime;

  /** 提交时间 */
  private Date submitTime;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  private static final long serialVersionUID = 1L;
}
