package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 每日挑战 */
@TableName(value = "daily_challenge")
@Data
public class DailyChallenge implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 挑战日期 */
  private Date challengeDate;

  /** 题目ID */
  private Long questionId;

  /** 挑战类型 (daily/weekly/special) */
  private String challengeType;

  /** 完成奖励积分 */
  private Integer bonusPoints;

  /** 建议难度 */
  private String difficulty;

  /** 挑战描述 */
  private String description;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
