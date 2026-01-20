package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.entity.ContestParticipant;

/** 比赛参与者服务 */
public interface ContestParticipantService extends IService<ContestParticipant> {

  /**
   * 用户参加比赛
   *
   * @param contestId 比赛ID
   * @param userId 用户ID
   * @return 参与者ID
   */
  Long joinContest(Long contestId, Long userId);

  /**
   * 检查用户是否已参加比赛
   *
   * @param contestId 比赛ID
   * @param userId 用户ID
   * @return 是否已参加
   */
  Boolean hasJoined(Long contestId, Long userId);

  /**
   * 获取比赛的参与者数量
   *
   * @param contestId 比赛ID
   * @return 参与者数量
   */
  Integer getParticipantCount(Long contestId);

  /**
   * 更新参与者统计信息（题目通过后调用）
   *
   * @param contestId 比赛ID
   * @param userId 用户ID
   * @param score 得分
   * @param timeUsed 用时（秒）
   */
  void updateParticipantStats(Long contestId, Long userId, Integer score, Long timeUsed);

  /**
   * 获取比赛的参与者列表
   *
   * @param contestId 比赛ID
   * @return 参与者列表
   */
  List<ContestParticipant> getContestParticipants(Long contestId);
}
