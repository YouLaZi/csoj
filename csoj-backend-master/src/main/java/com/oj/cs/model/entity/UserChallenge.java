package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 用户挑战参与 */
@TableName(value = "user_challenge")
@Data
public class UserChallenge implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 挑战ID */
  private Long challengeId;

  /** 是否完成 */
  private Integer isCompleted;

  /** 是否完美通关(首次提交即通过) */
  private Integer isPerfect;

  /** 尝试次数 */
  private Integer attemptCount;

  /** 获得积分 */
  private Integer pointsEarned;

  /** 完成时间 */
  private Date completedTime;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
