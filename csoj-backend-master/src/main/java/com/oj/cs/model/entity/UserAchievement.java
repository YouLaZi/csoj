package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 用户成就 */
@TableName(value = "user_achievement")
@Data
public class UserAchievement implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 成就代码 */
  private String achievementCode;

  /** 当前进度 */
  private Integer progress;

  /** 是否解锁 */
  private Integer isUnlocked;

  /** 解锁时间 */
  private Date unlockedTime;

  /** 是否新成就(未查看) */
  private Integer isNew;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
