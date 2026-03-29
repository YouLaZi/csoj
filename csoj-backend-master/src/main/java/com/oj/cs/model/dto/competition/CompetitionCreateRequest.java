package com.oj.cs.model.dto.competition;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/** 创建比赛请求 */
@Data
public class CompetitionCreateRequest implements Serializable {

  /** 比赛名称 */
  private String name;

  /** 比赛描述 */
  private String description;

  /** 比赛类型: elimination(淘汰赛), round_robin(循环赛), team_ac(团队AC赛), battle(对战), relay(接力) */
  private String type;

  /** 比赛封面图 */
  private String cover;

  /** 最大参赛队伍数 */
  private Integer maxTeams;

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

  /** 关联的题目ID列表 */
  private String questionIds;

  private static final long serialVersionUID = 1L;
}
