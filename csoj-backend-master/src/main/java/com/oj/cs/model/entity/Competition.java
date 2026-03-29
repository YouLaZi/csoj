package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 竞赛实体 */
@TableName("competition")
@Data
public class Competition implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 比赛名称 */
  private String name;

  /** 比赛描述 */
  private String description;

  /** 比赛类型: elimination(淘汰赛), round_robin(循环赛), team_ac(团队AC赛), battle(对战), relay(接力) */
  private String type;

  /** 比赛状态: 0-未开始, 1-报名中, 2-进行中, 3-已结束 */
  private Integer status;

  /** 比赛封面图 */
  private String cover;

  /** 创建者ID */
  private Long creatorId;

  /** 最大参赛队伍数 */
  private Integer maxTeams;

  /** 当前报名队伍数 */
  private Integer currentTeams;

  /** 每队最少人数 */
  private Integer minTeamSize;

  /** 每队最多人数 */
  private Integer maxTeamSize;

  /** 报名开始时间 */
  private Date registerStartTime;

  /** 报名结束时间 */
  private Date registerEndTime;

  /** 比赛开始时间 */
  private Date startTime;

  /** 比赛结束时间 */
  private Date endTime;

  /** 单场时间限制(分钟) */
  private Integer timeLimit;

  /** 是否公开 */
  private Integer isPublic;

  /** 关联的题目ID列表(JSON) */
  private String questionIds;

  /** 创建时间 */
  @TableField(fill = FieldFill.INSERT)
  private Date createTime;

  /** 更新时间 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  private static final long serialVersionUID = 1L;
}
