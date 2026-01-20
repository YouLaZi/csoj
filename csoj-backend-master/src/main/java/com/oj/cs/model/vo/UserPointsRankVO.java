package com.oj.cs.model.vo;

import java.io.Serializable;

import lombok.Data;

/** 用户积分排行榜视图 */
@Data
public class UserPointsRankVO implements Serializable {

  /** 用户ID */
  private Long userId;

  /** 用户昵称 */
  private String userName;

  /** 用户头像 */
  private String userAvatar;

  /** 总积分 */
  private Integer totalPoints;

  /** 排名 */
  private Integer rank;

  private static final long serialVersionUID = 1L;
}
