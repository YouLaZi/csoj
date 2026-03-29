package com.oj.cs.model.dto.team;

import java.io.Serializable;

import lombok.Data;

/** 创建团队请求 */
@Data
public class TeamCreateRequest implements Serializable {

  /** 队伍名称 */
  private String name;

  /** 队伍简介 */
  private String description;

  /** 队伍头像URL */
  private String avatar;

  /** 最大成员数（默认5人） */
  private Integer maxMembers;

  /** 是否公开招募（默认1） */
  private Integer isPublic;

  private static final long serialVersionUID = 1L;
}
