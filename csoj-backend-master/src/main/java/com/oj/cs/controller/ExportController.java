package com.oj.cs.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.model.dto.export.DataExportRequest;
import com.oj.cs.service.ExportService;

import lombok.extern.slf4j.Slf4j;

/** 数据导出接口 */
@RestController
@RequestMapping("/export")
@Slf4j
public class ExportController {

  @Resource private ExportService exportService;

  /**
   * 导出比赛排名
   *
   * @param contestId 比赛ID
   * @param format 导出格式 (excel, csv)
   * @param response HTTP响应
   */
  @GetMapping("/contest/{contestId}/ranking")
  public void exportContestRanking(
      @PathVariable Long contestId,
      @RequestParam(defaultValue = "excel") String format,
      HttpServletResponse response) {
    try {
      exportService.exportContestRanking(contestId, format, response);
    } catch (Exception e) {
      log.error("导出比赛排名失败, contestId={}", contestId, e);
      throw new RuntimeException("导出失败: " + e.getMessage());
    }
  }

  /**
   * 导出用户成绩
   *
   * @param userId 用户ID
   * @param format 导出格式
   * @param response HTTP响应
   */
  @GetMapping("/user/{userId}/scores")
  public void exportUserScores(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "excel") String format,
      HttpServletResponse response) {
    try {
      exportService.exportUserScores(userId, format, response);
    } catch (Exception e) {
      log.error("导出用户成绩失败, userId={}", userId, e);
      throw new RuntimeException("导出失败: " + e.getMessage());
    }
  }

  /**
   * 导出题目统计数据
   *
   * @param request 导出请求
   * @param response HTTP响应
   */
  @PostMapping("/question/stats")
  public void exportQuestionStats(
      @RequestBody DataExportRequest request, HttpServletResponse response) {
    try {
      exportService.exportQuestionStats(request, response);
    } catch (Exception e) {
      log.error("导出题目统计失败", e);
      throw new RuntimeException("导出失败: " + e.getMessage());
    }
  }

  /**
   * 导出判题记录
   *
   * @param request 导出请求
   * @param response HTTP响应
   */
  @PostMapping("/submit/records")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public void exportSubmitRecords(
      @RequestBody DataExportRequest request, HttpServletResponse response) {
    try {
      exportService.exportSubmitRecords(request, response);
    } catch (Exception e) {
      log.error("导出判题记录失败", e);
      throw new RuntimeException("导出失败: " + e.getMessage());
    }
  }

  /**
   * 导出学习报告
   *
   * @param userId 用户ID
   * @param reportType 报告类型 (daily, weekly, monthly)
   * @param response HTTP响应
   */
  @GetMapping("/learning/{userId}/report")
  public void exportLearningReport(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "daily") String reportType,
      HttpServletResponse response) {
    try {
      exportService.exportLearningReport(userId, reportType, response);
    } catch (Exception e) {
      log.error("导出学习报告失败, userId={}", userId, e);
      throw new RuntimeException("导出失败: " + e.getMessage());
    }
  }

  /**
   * 导出代码查重报告
   *
   * @param questionId 题目ID
   * @param response HTTP响应
   */
  @GetMapping("/similarity/{questionId}/report")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public void exportSimilarityReport(@PathVariable Long questionId, HttpServletResponse response) {
    try {
      exportService.exportSimilarityReport(questionId, response);
    } catch (Exception e) {
      log.error("导出查重报告失败, questionId={}", questionId, e);
      throw new RuntimeException("导出失败: " + e.getMessage());
    }
  }

  /**
   * 通用数据导出接口
   *
   * @param request 导出请求
   * @param response HTTP响应
   */
  @PostMapping("/data")
  public void exportData(@RequestBody DataExportRequest request, HttpServletResponse response) {
    try {
      exportService.exportData(request, response);
    } catch (Exception e) {
      log.error("导出数据失败, exportType={}", request.getExportType(), e);
      throw new RuntimeException("导出失败: " + e.getMessage());
    }
  }

  /**
   * 获取支持的导出类型列表
   *
   * @return 导出类型列表
   */
  @GetMapping("/types")
  public BaseResponse<java.util.List<ExportTypeInfo>> getExportTypes() {
    java.util.List<ExportTypeInfo> types = new java.util.ArrayList<>();
    types.add(new ExportTypeInfo("contest_ranking", "比赛排名"));
    types.add(new ExportTypeInfo("user_scores", "用户成绩"));
    types.add(new ExportTypeInfo("question_stats", "题目统计"));
    types.add(new ExportTypeInfo("submit_records", "判题记录"));
    types.add(new ExportTypeInfo("learning_report", "学习报告"));
    types.add(new ExportTypeInfo("similarity_report", "代码查重报告"));
    return ResultUtils.success(types);
  }

  /** 导出类型信息 */
  public static class ExportTypeInfo {
    private String code;
    private String name;

    public ExportTypeInfo(String code, String name) {
      this.code = code;
      this.name = name;
    }

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
