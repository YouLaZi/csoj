package com.oj.cs.model.dto.competition;

import java.io.Serializable;

import lombok.Data;

/** 比赛报名请求 */
@Data
public class CompetitionRegisterRequest implements Serializable {

  /** 比赛ID */
  private Long competitionId;

  /** 团队ID */
  private Long teamId;

  private static final long serialVersionUID = 1L;
}
