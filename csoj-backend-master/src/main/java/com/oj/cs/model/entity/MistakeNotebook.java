package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 错题本实体 */
@TableName(value = "mistake_notebook")
@Data
public class MistakeNotebook implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 用户ID */
  private Long userId;

  /** 题目ID */
  private Long questionId;

  /** 提交记录ID */
  private Long submitId;

  /** 错误类型 */
  private String mistakeType;

  /** 错误信息 */
  private String errorMessage;

  /** 用户提交的代码 */
  private String userCode;

  /** 用户笔记 */
  private String notes;

  /** 错题分类 */
  private String category;

  /** 是否已复习 */
  private Integer isReviewed;

  /** 复习次数 */
  private Integer reviewCount;

  /** 最后复习时间 */
  private Date lastReviewTime;

  /** 提醒复习时间 */
  private Date remindTime;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  /** 错误类型枚举 */
  public enum MistakeType {
    /** 编译错误 */
    COMPILE_ERROR,

    /** 运行时错误 */
    RUNTIME_ERROR,

    /** 答案错误 */
    WRONG_ANSWER,

    /** 超时 */
    TIME_LIMIT_EXCEEDED,

    /** 内存超限 */
    MEMORY_LIMIT_EXCEEDED
  }
}
