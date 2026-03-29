package com.oj.cs.service;

import java.time.LocalDate;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.entity.ChallengeStreak;
import com.oj.cs.model.entity.DailyChallenge;
import com.oj.cs.model.entity.UserChallenge;

/** 每日挑战系统服务 */
public interface DailyChallengeService extends IService<DailyChallenge> {

  /**
   * 获取今日挑战
   *
   * @return 今日挑战
   */
  DailyChallenge getTodayChallenge();

  /**
   * 获取指定日期的挑战
   *
   * @param date 日期
   * @return 挑战
   */
  DailyChallenge getChallengeByDate(LocalDate date);

  /**
   * 完成挑战
   *
   * @param userId 用户ID
   * @param challengeId 挑战ID
   * @param isPerfect 是否完美通关
   * @return 获得的积分
   */
  int completeChallenge(Long userId, Long challengeId, boolean isPerfect);

  /**
   * 获取用户挑战状态
   *
   * @param userId 用户ID
   * @param challengeId 挑战ID
   * @return 用户挑战记录
   */
  UserChallenge getUserChallengeStatus(Long userId, Long challengeId);

  /**
   * 获取用户连胜信息
   *
   * @param userId 用户ID
   * @return 连胜信息
   */
  ChallengeStreak getUserStreak(Long userId);

  /**
   * 获取挑战历史
   *
   * @param userId 用户ID
   * @param days 天数
   * @return 挑战历史列表
   */
  List<UserChallenge> getChallengeHistory(Long userId, int days);

  /**
   * 生成每日挑战(管理员或定时任务调用)
   *
   * @return 生成的挑战
   */
  DailyChallenge generateDailyChallenge();

  /**
   * 检查题目是否为今日挑战
   *
   * @param questionId 题目ID
   * @return 是否为今日挑战
   */
  boolean isTodayChallenge(Long questionId);

  /**
   * 处理题目提交事件
   *
   * @param userId 用户ID
   * @param questionId 题目ID
   * @param accepted 是否通过
   */
  void onQuestionSubmit(Long userId, Long questionId, boolean accepted);
}
