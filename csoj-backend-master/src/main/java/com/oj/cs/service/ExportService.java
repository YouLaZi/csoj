package com.oj.cs.service;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import com.oj.cs.model.dto.export.DataExportRequest;

/** 数据导出服务 */
public interface ExportService {

  /**
   * 导出比赛排名数据
   *
   * @param contestId 比赛ID
   * @param format 导出格式 (excel, csv)
   * @param response HTTP响应
   * @throws IOException IO异常
   */
  void exportContestRanking(Long contestId, String format, HttpServletResponse response)
      throws IOException;

  /**
   * 导出用户成绩数据
   *
   * @param userId 用户ID
   * @param format 导出格式
   * @param response HTTP响应
   * @throws IOException IO异常
   */
  void exportUserScores(Long userId, String format, HttpServletResponse response)
      throws IOException;

  /**
   * 导出题目统计数据
   *
   * @param request 导出请求
   * @param response HTTP响应
   * @throws IOException IO异常
   */
  void exportQuestionStats(DataExportRequest request, HttpServletResponse response)
      throws IOException;

  /**
   * 导出判题记录
   *
   * @param request 导出请求
   * @param response HTTP响应
   * @throws IOException IO异常
   */
  void exportSubmitRecords(DataExportRequest request, HttpServletResponse response)
      throws IOException;

  /**
   * 导出学习报告
   *
   * @param userId 用户ID
   * @param reportType 报告类型 (daily, weekly, monthly)
   * @param response HTTP响应
   * @throws IOException IO异常
   */
  void exportLearningReport(Long userId, String reportType, HttpServletResponse response)
      throws IOException;

  /**
   * 导出代码查重报告
   *
   * @param questionId 题目ID
   * @param response HTTP响应
   * @throws IOException IO异常
   */
  void exportSimilarityReport(Long questionId, HttpServletResponse response) throws IOException;

  /**
   * 通用数据导出方法
   *
   * @param request 导出请求
   * @param response HTTP响应
   * @throws IOException IO异常
   */
  void exportData(DataExportRequest request, HttpServletResponse response) throws IOException;
}
