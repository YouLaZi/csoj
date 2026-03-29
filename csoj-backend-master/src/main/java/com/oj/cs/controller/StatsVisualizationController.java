package com.oj.cs.controller;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.entity.ProblemHeatmap;
import com.oj.cs.model.entity.SkillProgress;
import com.oj.cs.model.entity.UserStatsCache;
import com.oj.cs.service.StatsVisualizationService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 数据可视化接口 */
@RestController
@RequestMapping("/stats")
@Slf4j
public class StatsVisualizationController {

  @Resource private StatsVisualizationService statsVisualizationService;

  @Resource private UserService userService;

  /** 获取热力图数据 */
  @GetMapping("/heatmap")
  public BaseResponse<List<ProblemHeatmap>> getHeatmap(
      @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
      HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    List<ProblemHeatmap> heatmap = statsVisualizationService.getHeatmapData(userId, year);
    return ResultUtils.success(heatmap);
  }

  /** 获取技能雷达数据 */
  @GetMapping("/skills")
  public BaseResponse<List<SkillProgress>> getSkillRadar(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    List<SkillProgress> skills = statsVisualizationService.getSkillRadar(userId);
    return ResultUtils.success(skills);
  }

  /** 获取进度曲线 */
  @GetMapping("/progress")
  public BaseResponse<Map<String, Object>> getProgressCurve(
      @RequestParam(defaultValue = "month") String period, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    Map<String, Object> progress = statsVisualizationService.getProgressCurve(userId, period);
    return ResultUtils.success(progress);
  }

  /** 获取综合统计 */
  @GetMapping("/comprehensive")
  public BaseResponse<UserStatsCache> getComprehensiveStats(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserStatsCache stats = statsVisualizationService.getComprehensiveStats(userId);
    return ResultUtils.success(stats);
  }

  /** 获取排名位置 */
  @GetMapping("/rank")
  public BaseResponse<Map<String, Object>> getLeaderboardPosition(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    Map<String, Object> position = statsVisualizationService.getLeaderboardPosition(userId);
    return ResultUtils.success(position);
  }

  /** 获取周统计 */
  @GetMapping("/weekly")
  public BaseResponse<Map<String, Object>> getWeeklyStats(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    Map<String, Object> stats = statsVisualizationService.getWeeklyStats(userId);
    return ResultUtils.success(stats);
  }

  /** 获取月统计 */
  @GetMapping("/monthly")
  public BaseResponse<Map<String, Object>> getMonthlyStats(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    Map<String, Object> stats = statsVisualizationService.getMonthlyStats(userId);
    return ResultUtils.success(stats);
  }

  /** 刷新统计缓存 */
  @PostMapping("/refresh")
  public BaseResponse<Boolean> refreshStats(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    statsVisualizationService.updateStatsCache(userId);
    return ResultUtils.success(true);
  }
}
