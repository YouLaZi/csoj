package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 比赛视图对象 */
@Data
public class CompetitionVO implements Serializable {

  /** 比赛ID */
  private Long id;

  /** 比赛名称 */
  private String name;

  /** 比赛描述 */
  private String description;

  /** 比赛类型 */
  private String type;

  /** 比赛类型名称 */
  private String typeName;

  /** 比赛封面图 */
  private String cover;

  /** 创建者ID */
  private Long creatorId;

  /** 创建者信息 */
  private UserVO creator;

  /** 最大参赛队伍数 */
  private Integer maxTeams;

  /** 当前参赛队伍数 */
  private Integer currentTeams;

  /** 每队最少人数 */
  private Integer minTeamSize;

  /** 每队最多人数 */
  private Integer maxTeamSize;

  /** 比赛状态: 0-未开始, 1-报名中, 2-进行中, 3-已结束 */
  private Integer status;

  /** 状态名称 */
  private String statusName;

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

  /** 关联的题目列表 */
  private List<QuestionVO> questions;

  /** 当前用户是否已报名 */
  private Boolean hasRegistered;

  /** 当前用户报名的团队 */
  private TeamVO registeredTeam;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  private static final long serialVersionUID = 1L;
}
