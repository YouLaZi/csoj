package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 团队视图对象 */
@Data
public class TeamVO implements Serializable {

  /** 队伍ID */
  private Long id;

  /** 队伍名称 */
  private String name;

  /** 队伍简介 */
  private String description;

  /** 队伍头像URL */
  private String avatar;

  /** 最大成员数 */
  private Integer maxMembers;

  /** 当前成员数 */
  private Integer currentMembers;

  /** 是否公开招募 */
  private Integer isPublic;

  /** 邀请码（仅队长可见） */
  private String inviteCode;

  /** 队长用户ID */
  private Long leaderId;

  /** 队长用户信息 */
  private UserVO leader;

  /** 成员列表 */
  private List<TeamMemberVO> members;

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

  /** 排名 */
  private Integer rank;

  /** 创建时间 */
  private Date createTime;

  /** 当前用户是否已加入 */
  private Boolean hasJoined;

  /** 当前用户在队伍中的角色 */
  private String userRole;

  private static final long serialVersionUID = 1L;
}
