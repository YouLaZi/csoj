package com.oj.cs.service;

import com.oj.cs.model.vo.LeaderboardVO;

/** 排行榜服务 */
public interface LeaderboardService {

  /**
   * 获取积分排行榜
   *
   * @param timeRange 时间范围（day-日榜，week-周榜，month-月榜，all-总榜）
   * @return 排行榜数据
   */
  LeaderboardVO getPointsLeaderboard(String timeRange);
}
