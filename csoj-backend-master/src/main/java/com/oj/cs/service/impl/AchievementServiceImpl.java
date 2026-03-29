package com.oj.cs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.mapper.AchievementMapper;
import com.oj.cs.mapper.UserAchievementMapper;
import com.oj.cs.model.entity.Achievement;
import com.oj.cs.model.entity.UserAchievement;
import com.oj.cs.service.AchievementService;
import com.oj.cs.service.PointsService;

import lombok.extern.slf4j.Slf4j;

/** 成就系统服务实现 */
@Service
@Slf4j
public class AchievementServiceImpl extends ServiceImpl<UserAchievementMapper, UserAchievement>
    implements AchievementService {

  @Resource private UserAchievementMapper userAchievementMapper;

  @Resource private AchievementMapper achievementMapper;

  @Resource private PointsService pointsService;

  @Override
  public List<UserAchievement> getUserAchievements(Long userId) {
    // 获取所有成就定义
    List<Achievement> allAchievements = getAllAchievements();

    // 获取用户已解锁的成就
    LambdaQueryWrapper<UserAchievement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserAchievement::getUserId, userId);
    List<UserAchievement> userAchievements = userAchievementMapper.selectList(queryWrapper);

    // 构建结果列表
    List<UserAchievement> result = new ArrayList<>();
    for (Achievement achievement : allAchievements) {
      UserAchievement ua =
          userAchievements.stream()
              .filter(u -> u.getAchievementCode().equals(achievement.getAchievementCode()))
              .findFirst()
              .orElse(null);

      if (ua == null) {
        // 创建未解锁的成就记录
        ua = new UserAchievement();
        ua.setUserId(userId);
        ua.setAchievementCode(achievement.getAchievementCode());
        ua.setProgress(0);
        ua.setIsUnlocked(0);
        ua.setIsNew(0);
      }
      result.add(ua);
    }

    return result;
  }

  @Override
  public List<UserAchievement> getUserAchievementsByCategory(Long userId, String category) {
    List<UserAchievement> allAchievements = getUserAchievements(userId);
    return allAchievements.stream()
        .filter(
            ua -> {
              Achievement def = getAchievementDefinition(ua.getAchievementCode());
              return def != null && category.equals(def.getCategory());
            })
        .toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateProgress(Long userId, String achievementCode, int value) {
    UserAchievement ua = getOrCreateUserAchievement(userId, achievementCode);
    if (ua.getIsUnlocked() == 1) {
      return false; // 已解锁
    }

    ua.setProgress(value);

    Achievement achievement = getAchievementDefinition(achievementCode);
    if (achievement != null && value >= achievement.getRequirement()) {
      // 解锁成就
      ua.setIsUnlocked(1);
      ua.setUnlockedTime(new Date());
      ua.setIsNew(1);

      // 奖励积分
      if (achievement.getPoints() > 0) {
        pointsService.addPointsRecord(
            userId,
            achievement.getPoints(),
            "achievement",
            "解锁成就: " + achievement.getAchievementName(),
            null);
      }

      log.info("用户 {} 解锁成就: {}", userId, achievementCode);
      userAchievementMapper.updateById(ua);
      return true;
    }

    userAchievementMapper.updateById(ua);
    return false;
  }

  @Override
  public boolean incrementProgress(Long userId, String achievementCode) {
    UserAchievement ua = getOrCreateUserAchievement(userId, achievementCode);
    return updateProgress(userId, achievementCode, ua.getProgress() + 1);
  }

  @Override
  public boolean checkAndUnlock(Long userId, String achievementCode) {
    UserAchievement ua = getOrCreateUserAchievement(userId, achievementCode);
    Achievement achievement = getAchievementDefinition(achievementCode);

    if (achievement != null
        && ua.getProgress() >= achievement.getRequirement()
        && ua.getIsUnlocked() == 0) {
      return updateProgress(userId, achievementCode, ua.getProgress());
    }
    return false;
  }

  @Override
  public Achievement getAchievementDefinition(String achievementCode) {
    LambdaQueryWrapper<Achievement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Achievement::getAchievementCode, achievementCode);
    return achievementMapper.selectOne(queryWrapper);
  }

  @Override
  public List<Achievement> getAllAchievements() {
    LambdaQueryWrapper<Achievement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.orderByAsc(Achievement::getCategory).orderByAsc(Achievement::getSortOrder);
    return achievementMapper.selectList(queryWrapper);
  }

  @Override
  public int getUnreadCount(Long userId) {
    LambdaQueryWrapper<UserAchievement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserAchievement::getUserId, userId);
    queryWrapper.eq(UserAchievement::getIsNew, 1);
    return Math.toIntExact(userAchievementMapper.selectCount(queryWrapper));
  }

  @Override
  public void markAsRead(Long userId, String achievementCode) {
    LambdaQueryWrapper<UserAchievement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserAchievement::getUserId, userId);
    queryWrapper.eq(UserAchievement::getAchievementCode, achievementCode);
    UserAchievement ua = userAchievementMapper.selectOne(queryWrapper);

    if (ua != null && ua.getIsNew() == 1) {
      ua.setIsNew(0);
      userAchievementMapper.updateById(ua);
    }
  }

  @Override
  public void onProblemSolved(Long userId, Long questionId, boolean isFirstAccept) {
    // 更新刷题相关成就
    incrementProgress(userId, "first_submit");

    if (isFirstAccept) {
      incrementProgress(userId, "first_accept");
      incrementProgress(userId, "perfect_score");
    }

    // 更新解决题目数成就
    incrementProgress(userId, "solve_10");
    incrementProgress(userId, "solve_50");
    incrementProgress(userId, "solve_100");
    incrementProgress(userId, "solve_500");
    incrementProgress(userId, "solve_1000");

    // 检查夜间刷题
    int hour = java.time.LocalTime.now().getHour();
    if (hour >= 0 && hour < 6) {
      incrementProgress(userId, "night_owl");
    } else if (hour >= 6 && hour < 8) {
      incrementProgress(userId, "early_bird");
    }

    // 检查周末战士
    java.time.DayOfWeek dayOfWeek = java.time.LocalDate.now().getDayOfWeek();
    if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
      incrementProgress(userId, "weekend_warrior");
    }

    // 猫咪成就
    incrementProgress(userId, "cat_feed_50");
    incrementProgress(userId, "cat_play_50");
  }

  @Override
  public void onCheckin(Long userId, int streakDays) {
    // 首次签到
    incrementProgress(userId, "checkin_first");

    // 连续签到成就
    updateProgress(userId, "checkin_7", streakDays);
    updateProgress(userId, "checkin_30", streakDays);
    updateProgress(userId, "checkin_100", streakDays);
    updateProgress(userId, "checkin_365", streakDays);
  }

  // ========== 私有方法 ==========

  private UserAchievement getOrCreateUserAchievement(Long userId, String achievementCode) {
    LambdaQueryWrapper<UserAchievement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserAchievement::getUserId, userId);
    queryWrapper.eq(UserAchievement::getAchievementCode, achievementCode);
    UserAchievement ua = userAchievementMapper.selectOne(queryWrapper);

    if (ua == null) {
      ua = new UserAchievement();
      ua.setUserId(userId);
      ua.setAchievementCode(achievementCode);
      ua.setProgress(0);
      ua.setIsUnlocked(0);
      ua.setIsNew(0);
      userAchievementMapper.insert(ua);
    }

    return ua;
  }
}
