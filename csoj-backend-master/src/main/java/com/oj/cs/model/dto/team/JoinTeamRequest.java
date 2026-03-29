package com.oj.cs.model.dto.team;

import java.io.Serializable;

import lombok.Data;

/** 加入团队请求 */
@Data
public class JoinTeamRequest implements Serializable {

  /** 团队ID */
  private Long teamId;

  /** 邀请码 */
  private String inviteCode;

  private static final long serialVersionUID = 1L;
}
