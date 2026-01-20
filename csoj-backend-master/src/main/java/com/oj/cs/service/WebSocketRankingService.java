package com.oj.cs.service;

import java.util.List;

import com.oj.cs.websocket.RankingWebSocketHandler;

/** WebSocket 排名推送服务 */
public interface WebSocketRankingService {

  /**
   * 推送比赛排名更新
   *
   * @param contestId 比赛ID
   * @param rankings 排名数据
   */
  void pushRankingUpdate(Long contestId, List<RankingWebSocketHandler.RankingItem> rankings);

  /**
   * 获取指定比赛的在线观看人数
   *
   * @param contestId 比赛ID
   * @return 在线人数
   */
  Integer getOnlineViewerCount(Long contestId);

  /**
   * 触发排名重新计算并推送 当有新的提交或评分变化时调用
   *
   * @param contestId 比赛ID
   * @param questionId 题目ID（可选，用于部分更新）
   */
  void triggerRankingRefresh(Long contestId, Long questionId);
}
