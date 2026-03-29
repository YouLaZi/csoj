package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 团队实体 */
@TableName("team")
@Data
public class Team implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 队伍名称 */
  private String name;

  /** 队伍简介 */
  private String description;

  /** 队伍头像URL */
  private String avatar;

  /** 最大成员数 */
  private Integer maxMembers;

  /** 是否公开招募 */
  private Integer isPublic;

  /** 邀请码 */
  private String inviteCode;

  /** 队长用户ID */
  private Long leaderId;

  /** 团队总积分 */
  private Long totalScore;

  /** 胜场数 */
  private Integer winCount;

  /** 负场数 */
  private Integer loseCount;

  /** 平局数 */
  private Integer drawCount;

  /** ELO积分 */
  private Integer rating;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;
}
