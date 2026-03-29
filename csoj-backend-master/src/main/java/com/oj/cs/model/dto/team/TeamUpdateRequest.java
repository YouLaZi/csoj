package com.oj.cs.model.dto.team;

import java.io.Serializable;

import lombok.Data;

/** 更新团队请求 */
@Data
public class TeamUpdateRequest implements Serializable {

  /** 团队ID */
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

  private static final long serialVersionUID = 1L;
}
