package com.oj.cs.model.vo;

import java.io.Serializable;

import lombok.Data;

/** 对阵参与者视图对象 */
@Data
public class MatchParticipantVO implements Serializable {

  /** ID */
  private Long id;

  /** 对阵ID */
  private Long matchId;

  /** 团队ID */
  private Long teamId;

  /** 用户ID */
  private Long userId;

  /** 用户信息 */
  private UserVO user;

  /** 解决题目数 */
  private Integer solvedCount;

  /** 总用时(毫秒) */
  private Long totalTime;

  /** 得分 */
  private Integer score;

  private static final long serialVersionUID = 1L;
}
