package com.oj.cs.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.entity.ProblemHeatmap;
import com.oj.cs.model.entity.SkillProgress;
import com.oj.cs.model.entity.UserStatsCache;

/** 数据可视化系统服务 */
public interface StatsVisualizationService extends IService<UserStatsCache> {

  /**
   * 获取热力图数据
   *
   * @param userId 用户ID
   * @param year 年份
   * @return 热力图数据列表
   */
  List<ProblemHeatmap> getHeatmapData(Long userId, int year);

  /**
   * 获取技能雷达数据
   *
   * @param userId 用户ID
   * @return 技能进度列表
   */
  List<SkillProgress> getSkillRadar(Long userId);

  /**
   * 获取进度曲线数据
   *
   * @param userId 用户ID
   * @param period 周期(week/month/year)
   * @return 进度数据
   */
  Map<String, Object> getProgressCurve(Long userId, String period);

  /**
   * 获取综合统计
   *
   * @param userId 用户ID
   * @return 统计缓存
   */
  UserStatsCache getComprehensiveStats(Long userId);

  /**
   * 更新统计缓存
   *
   * @param userId 用户ID
   */
  void updateStatsCache(Long userId);

  /**
   * 更新热力图
   *
   * @param userId 用户ID
   * @param solved 是否解决
   */
  void updateHeatmap(Long userId, boolean solved);

  /**
   * 更新技能进度
   *
   * @param userId 用户ID
   * @param tags 标签列表
   */
  void updateSkills(Long userId, List<String> tags);

  /**
   * 获取用户排名位置
   *
   * @param userId 用户ID
   * @return 排名信息
   */
  Map<String, Object> getLeaderboardPosition(Long userId);

  /**
   * 获取周统计
   *
   * @param userId 用户ID
   * @return 周统计数据
   */
  Map<String, Object> getWeeklyStats(Long userId);

  /**
   * 获取月统计
   *
   * @param userId 用户ID
   * @return 月统计数据
   */
  Map<String, Object> getMonthlyStats(Long userId);
}
