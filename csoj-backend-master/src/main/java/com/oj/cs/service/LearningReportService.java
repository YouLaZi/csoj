package com.oj.cs.service;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.learning.LearningReportGenerateRequest;
import com.oj.cs.model.entity.LearningReport;

/** 学习报告服务接口 */
public interface LearningReportService extends IService<LearningReport> {

  /**
   * 生成学习报告
   *
   * @param request 请求参数
   * @param userId 用户ID
   * @return 报告ID
   */
  Long generateReport(LearningReportGenerateRequest request, Long userId);

  /** 自动生成日报（定时任务调用） */
  void generateDailyReports();

  /** 自动生成周报（定时任务调用） */
  void generateWeeklyReports();

  /** 自动生成月报（定时任务调用） */
  void generateMonthlyReports();

  /** 获取用户最新的报告 */
  LearningReport getLatestReport(Long userId, String reportType);

  /** 获取用户最近的报告列表 */
  List<LearningReport> getRecentReports(Long userId, int limit);

  /** 获取指定时间范围的报告 */
  List<LearningReport> getReportsByDateRange(Long userId, Date startDate, Date endDate);

  /** 删除报告 */
  Boolean deleteReport(Long id, Long userId);

  /** 导出报告为图片/PDF（预留接口） */
  String exportReport(Long id, String format);
}
