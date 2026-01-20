package com.oj.cs.model.vo;

import java.io.Serializable;

import lombok.Data;

/** 排行榜用户信息视图 */
@Data
public class LeaderboardUserVO implements Serializable {

  /** 用户ID */
  private Long userId;

  /** 用户名 */
  private String userName;

  /** 积分 */
  private Integer points;

  /** 排名 */
  private Integer rank;

  private static final long serialVersionUID = 1L;
}
