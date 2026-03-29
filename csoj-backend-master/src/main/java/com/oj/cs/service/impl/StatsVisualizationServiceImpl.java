package com.oj.cs.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.mapper.ProblemHeatmapMapper;
import com.oj.cs.mapper.QuestionSubmitMapper;
import com.oj.cs.mapper.SkillProgressMapper;
import com.oj.cs.mapper.UserStatsCacheMapper;
import com.oj.cs.model.entity.ProblemHeatmap;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.SkillProgress;
import com.oj.cs.model.entity.UserStatsCache;
import com.oj.cs.service.StatsVisualizationService;

import lombok.extern.slf4j.Slf4j;

/** 数据可视化系统服务实现 */
@Service
@Slf4j
public class StatsVisualizationServiceImpl extends ServiceImpl<UserStatsCacheMapper, UserStatsCache>
    implements StatsVisualizationService {

  @Resource private UserStatsCacheMapper userStatsCacheMapper;

  @Resource private ProblemHeatmapMapper problemHeatmapMapper;

  @Resource private SkillProgressMapper skillProgressMapper;

  @Resource private QuestionSubmitMapper questionSubmitMapper;

  @Override
  public List<ProblemHeatmap> getHeatmapData(Long userId, int year) {
    LocalDate startDate = LocalDate.of(year, 1, 1);
    LocalDate endDate = LocalDate.of(year, 12, 31);

    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    LambdaQueryWrapper<ProblemHeatmap> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ProblemHeatmap::getUserId, userId);
    queryWrapper.ge(ProblemHeatmap::getDate, start);
    queryWrapper.lt(ProblemHeatmap::getDate, end);
    queryWrapper.orderByAsc(ProblemHeatmap::getDate);

    return problemHeatmapMapper.selectList(queryWrapper);
  }

  @Override
  public List<SkillProgress> getSkillRadar(Long userId) {
    LambdaQueryWrapper<SkillProgress> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(SkillProgress::getUserId, userId);
    queryWrapper.orderByDesc(SkillProgress::getProblemsSolved);
    queryWrapper.last("LIMIT 8");

    return skillProgressMapper.selectList(queryWrapper);
  }

  @Override
  public Map<String, Object> getProgressCurve(Long userId, String period) {
    Map<String, Object> result = new HashMap<>();
    List<String> labels = new ArrayList<>();
    List<Integer> solvedCounts = new ArrayList<>();
    List<Integer> submitCounts = new ArrayList<>();

    LocalDate endDate = LocalDate.now();
    LocalDate startDate;

    switch (period) {
      case "week":
        startDate = endDate.minusDays(7);
        break;
      case "month":
        startDate = endDate.minusDays(30);
        break;
      case "year":
        startDate = endDate.minusDays(365);
        break;
      default:
        startDate = endDate.minusDays(30);
    }

    // 获取时间范围内的热力图数据
    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    LambdaQueryWrapper<ProblemHeatmap> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ProblemHeatmap::getUserId, userId);
    queryWrapper.ge(ProblemHeatmap::getDate, start);
    queryWrapper.lt(ProblemHeatmap::getDate, end);
    queryWrapper.orderByAsc(ProblemHeatmap::getDate);

    List<ProblemHeatmap> heatmapData = problemHeatmapMapper.selectList(queryWrapper);

    // 构建日期映射
    Map<LocalDate, ProblemHeatmap> dateMap = new HashMap<>();
    for (ProblemHeatmap ph : heatmapData) {
      LocalDate date = ph.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      dateMap.put(date, ph);
    }

    // 填充数据
    int cumulativeSolved = 0;
    int cumulativeSubmit = 0;
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      labels.add(date.toString());

      ProblemHeatmap ph = dateMap.get(date);
      if (ph != null) {
        cumulativeSolved += ph.getSolvedCount();
        cumulativeSubmit += ph.getSubmitCount();
      }

      solvedCounts.add(cumulativeSolved);
      submitCounts.add(cumulativeSubmit);
    }

    result.put("labels", labels);
    result.put("solvedCounts", solvedCounts);
    result.put("submitCounts", submitCounts);

    return result;
  }

  @Override
  public UserStatsCache getComprehensiveStats(Long userId) {
    UserStatsCache cache = getOrCreateStatsCache(userId);

    // 检查是否需要更新
    long hoursSinceUpdate = 0;
    if (cache.getLastUpdated() != null) {
      hoursSinceUpdate =
          (System.currentTimeMillis() - cache.getLastUpdated().getTime()) / (1000 * 60 * 60);
    }

    if (hoursSinceUpdate >= 1) {
      updateStatsCache(userId);
      cache = getOrCreateStatsCache(userId);
    }

    return cache;
  }

  @Override
  public void updateStatsCache(Long userId) {
    UserStatsCache cache = getOrCreateStatsCache(userId);

    // 统计提交数据
    LambdaQueryWrapper<QuestionSubmit> submitWrapper = new LambdaQueryWrapper<>();
    submitWrapper.eq(QuestionSubmit::getUserId, userId);
    long totalSubmissions = questionSubmitMapper.selectCount(submitWrapper);

    // 统计通过数据 (status = 2 表示成功)
    LambdaQueryWrapper<QuestionSubmit> acceptWrapper = new LambdaQueryWrapper<>();
    acceptWrapper.eq(QuestionSubmit::getUserId, userId);
    acceptWrapper.eq(QuestionSubmit::getStatus, 2);
    long acceptedSubmissions = questionSubmitMapper.selectCount(acceptWrapper);

    // 计算通过率
    BigDecimal acceptRate = BigDecimal.ZERO;
    if (totalSubmissions > 0) {
      acceptRate =
          BigDecimal.valueOf(acceptedSubmissions * 100.0 / totalSubmissions)
              .setScale(2, RoundingMode.HALF_UP);
    }

    // 更新缓存
    cache.setTotalSolved((int) acceptedSubmissions);
    cache.setTotalSubmissions((int) totalSubmissions);
    cache.setAcceptRate(acceptRate);
    cache.setLastUpdated(new Date());

    userStatsCacheMapper.updateById(cache);
    log.info("更新用户 {} 的统计缓存", userId);
  }

  @Override
  public void updateHeatmap(Long userId, boolean solved) {
    LocalDate today = LocalDate.now();
    Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

    LambdaQueryWrapper<ProblemHeatmap> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ProblemHeatmap::getUserId, userId);
    queryWrapper.eq(ProblemHeatmap::getDate, todayDate);

    ProblemHeatmap heatmap = problemHeatmapMapper.selectOne(queryWrapper);

    if (heatmap == null) {
      heatmap = new ProblemHeatmap();
      heatmap.setUserId(userId);
      heatmap.setDate(todayDate);
      heatmap.setSolvedCount(solved ? 1 : 0);
      heatmap.setSubmitCount(1);
      problemHeatmapMapper.insert(heatmap);
    } else {
      heatmap.setSubmitCount(heatmap.getSubmitCount() + 1);
      if (solved) {
        heatmap.setSolvedCount(heatmap.getSolvedCount() + 1);
      }
      problemHeatmapMapper.updateById(heatmap);
    }
  }

  @Override
  public void updateSkills(Long userId, List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return;
    }

    for (String tag : tags) {
      if (tag == null || tag.trim().isEmpty()) {
        continue;
      }

      String skillCode = tag.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");

      LambdaQueryWrapper<SkillProgress> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(SkillProgress::getUserId, userId);
      queryWrapper.eq(SkillProgress::getSkillCode, skillCode);

      SkillProgress skill = skillProgressMapper.selectOne(queryWrapper);

      if (skill == null) {
        skill = new SkillProgress();
        skill.setUserId(userId);
        skill.setSkillCode(skillCode);
        skill.setSkillName(tag.trim());
        skill.setLevel(1);
        skill.setExperience(10);
        skill.setProblemsSolved(1);
        skill.setLastPracticeTime(new Date());
        skillProgressMapper.insert(skill);
      } else {
        skill.setExperience(skill.getExperience() + 10);
        skill.setProblemsSolved(skill.getProblemsSolved() + 1);
        skill.setLastPracticeTime(new Date());

        // 升级逻辑: 每100经验升1级
        int newLevel = skill.getExperience() / 100 + 1;
        if (newLevel > skill.getLevel()) {
          skill.setLevel(newLevel);
          log.info("用户 {} 技能 {} 升级到 {} 级", userId, skillCode, newLevel);
        }

        skillProgressMapper.updateById(skill);
      }
    }
  }

  @Override
  public Map<String, Object> getLeaderboardPosition(Long userId) {
    Map<String, Object> result = new HashMap<>();

    // 获取用户统计
    UserStatsCache cache = getComprehensiveStats(userId);

    // 计算排名 (基于解决题目数)
    LambdaQueryWrapper<UserStatsCache> rankWrapper = new LambdaQueryWrapper<>();
    rankWrapper.gt(UserStatsCache::getTotalSolved, cache.getTotalSolved());
    long rank = userStatsCacheMapper.selectCount(rankWrapper) + 1;

    result.put("rank", rank);
    result.put("totalSolved", cache.getTotalSolved());
    result.put("acceptRate", cache.getAcceptRate());

    return result;
  }

  @Override
  public Map<String, Object> getWeeklyStats(Long userId) {
    Map<String, Object> result = new HashMap<>();

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(7);

    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    LambdaQueryWrapper<ProblemHeatmap> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ProblemHeatmap::getUserId, userId);
    queryWrapper.ge(ProblemHeatmap::getDate, start);
    queryWrapper.lt(ProblemHeatmap::getDate, end);

    List<ProblemHeatmap> weekData = problemHeatmapMapper.selectList(queryWrapper);

    int totalSolved = weekData.stream().mapToInt(ProblemHeatmap::getSolvedCount).sum();
    int totalSubmit = weekData.stream().mapToInt(ProblemHeatmap::getSubmitCount).sum();
    int activeDays = weekData.size();

    result.put("totalSolved", totalSolved);
    result.put("totalSubmit", totalSubmit);
    result.put("activeDays", activeDays);
    result.put("avgSolvedPerDay", activeDays > 0 ? totalSolved * 1.0 / activeDays : 0);

    return result;
  }

  @Override
  public Map<String, Object> getMonthlyStats(Long userId) {
    Map<String, Object> result = new HashMap<>();

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(30);

    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date end = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    LambdaQueryWrapper<ProblemHeatmap> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ProblemHeatmap::getUserId, userId);
    queryWrapper.ge(ProblemHeatmap::getDate, start);
    queryWrapper.lt(ProblemHeatmap::getDate, end);

    List<ProblemHeatmap> monthData = problemHeatmapMapper.selectList(queryWrapper);

    int totalSolved = monthData.stream().mapToInt(ProblemHeatmap::getSolvedCount).sum();
    int totalSubmit = monthData.stream().mapToInt(ProblemHeatmap::getSubmitCount).sum();
    int activeDays = monthData.size();

    result.put("totalSolved", totalSolved);
    result.put("totalSubmit", totalSubmit);
    result.put("activeDays", activeDays);
    result.put("avgSolvedPerDay", activeDays > 0 ? totalSolved * 1.0 / activeDays : 0);

    return result;
  }

  // ========== 私有方法 ==========

  private UserStatsCache getOrCreateStatsCache(Long userId) {
    LambdaQueryWrapper<UserStatsCache> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserStatsCache::getUserId, userId);

    UserStatsCache cache = userStatsCacheMapper.selectOne(queryWrapper);

    if (cache == null) {
      cache = new UserStatsCache();
      cache.setUserId(userId);
      cache.setTotalSolved(0);
      cache.setTotalSubmissions(0);
      cache.setAcceptRate(BigDecimal.ZERO);
      cache.setCurrentStreak(0);
      cache.setMaxStreak(0);
      cache.setAvgTimePerProblem(0);
      cache.setLastUpdated(new Date());
      userStatsCacheMapper.insert(cache);
    }

    return cache;
  }
}
