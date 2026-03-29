package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.entity.Achievement;
import com.oj.cs.model.entity.UserAchievement;

/** 成就系统服务 */
public interface AchievementService extends IService<UserAchievement> {

  /**
   * 获取用户所有成就
   *
   * @param userId 用户ID
   * @return 成就列表(含进度)
   */
  List<UserAchievement> getUserAchievements(Long userId);

  /**
   * 获取用户指定分类的成就
   *
   * @param userId 用户ID
   * @param category 分类
   * @return 成就列表
   */
  List<UserAchievement> getUserAchievementsByCategory(Long userId, String category);

  /**
   * 更新成就进度
   *
   * @param userId 用户ID
   * @param achievementCode 成就代码
   * @param value 进度值
   * @return 是否解锁
   */
  boolean updateProgress(Long userId, String achievementCode, int value);

  /**
   * 增加成就进度
   *
   * @param userId 用户ID
   * @param achievementCode 成就代码
   * @return 是否解锁
   */
  boolean incrementProgress(Long userId, String achievementCode);

  /**
   * 检查并解锁成就
   *
   * @param userId 用户ID
   * @param achievementCode 成就代码
   * @return 是否新解锁
   */
  boolean checkAndUnlock(Long userId, String achievementCode);

  /**
   * 获取成就定义
   *
   * @param achievementCode 成就代码
   * @return 成就定义
   */
  Achievement getAchievementDefinition(String achievementCode);

  /**
   * 获取所有成就定义
   *
   * @return 成就定义列表
   */
  List<Achievement> getAllAchievements();

  /**
   * 获取用户未读成就数量
   *
   * @param userId 用户ID
   * @return 未读数量
   */
  int getUnreadCount(Long userId);

  /**
   * 标记成就为已读
   *
   * @param userId 用户ID
   * @param achievementCode 成就代码
   */
  void markAsRead(Long userId, String achievementCode);

  /**
   * 处理题目解决事件
   *
   * @param userId 用户ID
   * @param questionId 题目ID
   * @param isFirstAccept 是否首次通过
   */
  void onProblemSolved(Long userId, Long questionId, boolean isFirstAccept);

  /**
   * 处理签到事件
   *
   * @param userId 用户ID
   * @param streakDays 连续天数
   */
  void onCheckin(Long userId, int streakDays);
}
