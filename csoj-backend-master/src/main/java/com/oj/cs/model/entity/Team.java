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
  @TableField("max_members")
  private Integer maxMembers;

  /** 是否公开招募 */
  @TableField("is_public")
  private Integer isPublic;

  /** 邀请码 */
  @TableField("invite_code")
  private String inviteCode;

  /** 队长用户ID */
  @TableField("leader_id")
  private Long leaderId;

  /** 团队总积分 */
  @TableField("total_score")
  private Long totalScore;

  /** 胜场数 */
  @TableField("win_count")
  private Integer winCount;

  /** 负场数 */
  @TableField("lose_count")
  private Integer loseCount;

  /** 平局数 */
  @TableField("draw_count")
  private Integer drawCount;

  /** ELO积分 */
  private Integer rating;

  /** 创建时间 */
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private Date createTime;

  /** 更新时间 */
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  /** 是否删除 */
  @TableLogic
  @TableField("is_delete")
  private Integer isDelete;
}
