package com.oj.cs.controller;

import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.dto.learning.LearningReportGenerateRequest;
import com.oj.cs.model.entity.LearningReport;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.LearningReportService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 学习报告接口 */
@RestController
@RequestMapping("/learning/report")
@Slf4j
public class LearningReportController {

  @Resource private LearningReportService learningReportService;

  @Resource private UserService userService;

  /** 生成学习报告 */
  @PostMapping("/generate")
  public BaseResponse<Long> generateReport(
      @RequestBody LearningReportGenerateRequest request, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    Long reportId = learningReportService.generateReport(request, loginUser.getId());
    return ResultUtils.success(reportId);
  }

  /** 获取最新的报告 */
  @GetMapping("/latest")
  public BaseResponse<LearningReport> getLatestReport(
      @RequestParam(defaultValue = "DAILY") String reportType, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    LearningReport report = learningReportService.getLatestReport(loginUser.getId(), reportType);
    return ResultUtils.success(report);
  }

  /** 获取最近的报告列表 */
  @GetMapping("/recent")
  public BaseResponse<List<LearningReport>> getRecentReports(
      @RequestParam(defaultValue = "10") int limit, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    List<LearningReport> reports = learningReportService.getRecentReports(loginUser.getId(), limit);
    return ResultUtils.success(reports);
  }

  /** 获取指定时间范围的报告 */
  @GetMapping("/range")
  public BaseResponse<List<LearningReport>> getReportsByDateRange(
      @RequestParam Long startDate, @RequestParam Long endDate, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    List<LearningReport> reports =
        learningReportService.getReportsByDateRange(
            loginUser.getId(), new Date(startDate), new Date(endDate));
    return ResultUtils.success(reports);
  }

  /** 删除报告 */
  @PostMapping("/delete")
  public BaseResponse<Boolean> deleteReport(@RequestParam Long id, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    Boolean result = learningReportService.deleteReport(id, loginUser.getId());
    return ResultUtils.success(result);
  }

  /** 获取报告详情 */
  @GetMapping("/get")
  public BaseResponse<LearningReport> getReport(@RequestParam Long id) {
    LearningReport report = learningReportService.getById(id);
    return ResultUtils.success(report);
  }

  /** 导出报告（预留接口） */
  @GetMapping("/export")
  public BaseResponse<String> exportReport(
      @RequestParam Long id, @RequestParam(defaultValue = "pdf") String format) {
    String result = learningReportService.exportReport(id, format);
    return ResultUtils.success(result);
  }
}
