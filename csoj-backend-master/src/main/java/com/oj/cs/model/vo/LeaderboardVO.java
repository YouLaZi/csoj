package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 排行榜视图 */
@Data
public class LeaderboardVO implements Serializable {

  /** 时间范围 */
  private String timeRange;

  /** 用户列表 */
  private List<LeaderboardUserVO> users;

  private static final long serialVersionUID = 1L;
}
