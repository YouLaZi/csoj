package com.oj.cs.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.ChallengeStreakMapper;
import com.oj.cs.mapper.DailyChallengeMapper;
import com.oj.cs.mapper.QuestionMapper;
import com.oj.cs.mapper.UserChallengeMapper;
import com.oj.cs.model.entity.ChallengeStreak;
import com.oj.cs.model.entity.DailyChallenge;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.UserChallenge;
import com.oj.cs.service.DailyChallengeService;
import com.oj.cs.service.PointsService;

import lombok.extern.slf4j.Slf4j;

/** 每日挑战系统服务实现 */
@Service
@Slf4j
public class DailyChallengeServiceImpl extends ServiceImpl<DailyChallengeMapper, DailyChallenge>
    implements DailyChallengeService {

  @Resource private DailyChallengeMapper dailyChallengeMapper;

  @Resource private UserChallengeMapper userChallengeMapper;

  @Resource private ChallengeStreakMapper challengeStreakMapper;

  @Resource private QuestionMapper questionMapper;

  @Resource private PointsService pointsService;

  // 奖励常量
  private static final int BASE_BONUS = 30;
  private static final int PERFECT_BONUS = 20;
  private static final int STREAK_BONUS_PER_DAY = 5;
  private static final int STREAK_MILESTONE_7 = 50;
  private static final int STREAK_MILESTONE_30 = 200;

  @Override
  public DailyChallenge getTodayChallenge() {
    LocalDate today = LocalDate.now();
    return getChallengeByDate(today);
  }

  @Override
  public DailyChallenge getChallengeByDate(LocalDate date) {
    Date challengeDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

    LambdaQueryWrapper<DailyChallenge> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(DailyChallenge::getChallengeDate, challengeDate);
    queryWrapper.eq(DailyChallenge::getChallengeType, "daily");

    DailyChallenge challenge = dailyChallengeMapper.selectOne(queryWrapper);

    // 如果当天没有挑战，自动生成一个
    if (challenge == null && date.equals(LocalDate.now())) {
      challenge = generateDailyChallenge();
    }

    return challenge;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int completeChallenge(Long userId, Long challengeId, boolean isPerfect) {
    // 检查挑战是否存在
    DailyChallenge challenge = dailyChallengeMapper.selectById(challengeId);
    if (challenge == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "挑战不存在");
    }

    // 检查是否已完成
    UserChallenge existingChallenge = getUserChallengeStatus(userId, challengeId);
    if (existingChallenge != null && existingChallenge.getIsCompleted() == 1) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "今日挑战已完成");
    }

    // 计算积分
    int points = BASE_BONUS;
    if (isPerfect) {
      points += PERFECT_BONUS;
    }

    // 连胜奖励
    ChallengeStreak streak = getUserStreak(userId);
    points += streak.getCurrentStreak() * STREAK_BONUS_PER_DAY;

    // 连胜里程碑奖励
    if (streak.getCurrentStreak() == 7) {
      points += STREAK_MILESTONE_7;
    } else if (streak.getCurrentStreak() == 30) {
      points += STREAK_MILESTONE_30;
    }

    // 更新或创建用户挑战记录
    UserChallenge userChallenge;
    if (existingChallenge != null) {
      userChallenge = existingChallenge;
      userChallenge.setIsCompleted(1);
      userChallenge.setIsPerfect(isPerfect ? 1 : 0);
      userChallenge.setPointsEarned(points);
      userChallenge.setCompletedTime(new Date());
      userChallengeMapper.updateById(userChallenge);
    } else {
      userChallenge = new UserChallenge();
      userChallenge.setUserId(userId);
      userChallenge.setChallengeId(challengeId);
      userChallenge.setIsCompleted(1);
      userChallenge.setIsPerfect(isPerfect ? 1 : 0);
      userChallenge.setAttemptCount(1);
      userChallenge.setPointsEarned(points);
      userChallenge.setCompletedTime(new Date());
      userChallengeMapper.insert(userChallenge);
    }

    // 更新连胜
    updateStreak(userId, true);

    // 添加积分记录
    pointsService.addPointsRecord(
        userId, points, "daily_challenge", "完成每日挑战" + (isPerfect ? "(完美通关)" : ""), challengeId);

    log.info("用户 {} 完成每日挑战，获得 {} 积分", userId, points);
    return points;
  }

  @Override
  public UserChallenge getUserChallengeStatus(Long userId, Long challengeId) {
    LambdaQueryWrapper<UserChallenge> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserChallenge::getUserId, userId);
    queryWrapper.eq(UserChallenge::getChallengeId, challengeId);
    return userChallengeMapper.selectOne(queryWrapper);
  }

  @Override
  public ChallengeStreak getUserStreak(Long userId) {
    LambdaQueryWrapper<ChallengeStreak> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ChallengeStreak::getUserId, userId);
    ChallengeStreak streak = challengeStreakMapper.selectOne(queryWrapper);

    if (streak == null) {
      streak = new ChallengeStreak();
      streak.setUserId(userId);
      streak.setCurrentStreak(0);
      streak.setMaxStreak(0);
      streak.setTotalCompleted(0);
      challengeStreakMapper.insert(streak);
    }

    return streak;
  }

  @Override
  public List<UserChallenge> getChallengeHistory(Long userId, int days) {
    LocalDate startDate = LocalDate.now().minusDays(days);
    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

    LambdaQueryWrapper<UserChallenge> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserChallenge::getUserId, userId);
    queryWrapper.ge(UserChallenge::getCreateTime, start);
    queryWrapper.orderByDesc(UserChallenge::getCreateTime);
    return userChallengeMapper.selectList(queryWrapper);
  }

  @Override
  public DailyChallenge generateDailyChallenge() {
    // 检查今日是否已有挑战
    DailyChallenge existing = getTodayChallenge();
    if (existing != null) {
      return existing;
    }

    // 随机选择一道题目
    LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Question::getIsDelete, 0);
    queryWrapper.last("ORDER BY RAND() LIMIT 1");
    Question question = questionMapper.selectOne(queryWrapper);

    if (question == null) {
      log.warn("没有可用的题目来生成每日挑战");
      return null;
    }

    // 创建挑战
    DailyChallenge challenge = new DailyChallenge();
    challenge.setChallengeDate(
        Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    challenge.setQuestionId(question.getId());
    challenge.setChallengeType("daily");
    challenge.setBonusPoints(BASE_BONUS);
    challenge.setDifficulty(question.getDifficulty());
    challenge.setDescription("今日挑战: " + question.getTitle());

    dailyChallengeMapper.insert(challenge);
    log.info("生成每日挑战，题目ID: {}", question.getId());

    return challenge;
  }

  @Override
  public boolean isTodayChallenge(Long questionId) {
    DailyChallenge today = getTodayChallenge();
    return today != null && today.getQuestionId().equals(questionId);
  }

  @Override
  public void onQuestionSubmit(Long userId, Long questionId, boolean accepted) {
    // 检查是否为今日挑战
    if (!isTodayChallenge(questionId)) {
      return;
    }

    DailyChallenge today = getTodayChallenge();
    if (today == null) {
      return;
    }

    // 检查是否已完成
    UserChallenge userChallenge = getUserChallengeStatus(userId, today.getId());
    if (userChallenge != null && userChallenge.getIsCompleted() == 1) {
      return;
    }

    if (accepted) {
      // 判断是否完美通关(首次提交即通过)
      boolean isPerfect = userChallenge == null || userChallenge.getAttemptCount() == 0;
      completeChallenge(userId, today.getId(), isPerfect);
    } else {
      // 记录尝试次数
      if (userChallenge == null) {
        userChallenge = new UserChallenge();
        userChallenge.setUserId(userId);
        userChallenge.setChallengeId(today.getId());
        userChallenge.setAttemptCount(1);
        userChallengeMapper.insert(userChallenge);
      } else {
        userChallenge.setAttemptCount(userChallenge.getAttemptCount() + 1);
        userChallengeMapper.updateById(userChallenge);
      }
    }
  }

  // ========== 私有方法 ==========

  private void updateStreak(Long userId, boolean completed) {
    ChallengeStreak streak = getUserStreak(userId);
    LocalDate today = LocalDate.now();
    LocalDate lastDate = null;

    if (streak.getLastChallengeDate() != null) {
      lastDate =
          streak.getLastChallengeDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    if (completed) {
      if (lastDate == null || lastDate.isBefore(today.minusDays(1))) {
        // 开始新的连胜
        streak.setCurrentStreak(1);
      } else if (lastDate.equals(today.minusDays(1))) {
        // 继续连胜
        streak.setCurrentStreak(streak.getCurrentStreak() + 1);
      }
      // 今天已经完成过，不重复增加

      streak.setTotalCompleted(streak.getTotalCompleted() + 1);
      streak.setLastChallengeDate(
          Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));

      // 更新最大连胜
      if (streak.getCurrentStreak() > streak.getMaxStreak()) {
        streak.setMaxStreak(streak.getCurrentStreak());
      }

      challengeStreakMapper.updateById(streak);
    } else {
      // 检查连胜是否中断
      if (lastDate != null && lastDate.isBefore(today.minusDays(1))) {
        streak.setCurrentStreak(0);
        challengeStreakMapper.updateById(streak);
      }
    }
  }
}
