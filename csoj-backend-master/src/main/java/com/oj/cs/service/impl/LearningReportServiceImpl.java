package com.oj.cs.service.impl;

import java.util.*;

import jakarta.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.LearningReportMapper;
import com.oj.cs.model.dto.learning.LearningReportGenerateRequest;
import com.oj.cs.model.entity.LearningReport;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.LearningReportService;
import com.oj.cs.service.QuestionSubmitService;
import com.oj.cs.service.UserService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/** 学习报告服务实现 */
@Service
@Slf4j
public class LearningReportServiceImpl extends ServiceImpl<LearningReportMapper, LearningReport>
    implements LearningReportService {

  @Resource private UserService userService;

  @Resource private QuestionSubmitService questionSubmitService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long generateReport(LearningReportGenerateRequest request, Long userId) {
    if (userId == null || userId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    User user = userService.getById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
    }

    // 如果没有指定日期范围，根据报告类型自动设置
    Date startDate = request.getStartDate();
    Date endDate = request.getEndDate();
    String reportType = request.getReportType();

    if (startDate == null || endDate == null) {
      Date now = new Date();
      if ("DAILY".equals(reportType)) {
        endDate = now;
        startDate = DateUtil.offsetDay(now, -1);
      } else if ("WEEKLY".equals(reportType)) {
        endDate = now;
        startDate = DateUtil.offsetWeek(now, -1);
      } else if ("MONTHLY".equals(reportType)) {
        endDate = now;
        startDate = DateUtil.offsetMonth(now, -1);
      } else {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "请指定日期范围");
      }
    }

    // 查询该时间范围内的提交记录
    QueryWrapper<QuestionSubmit> submitWrapper = new QueryWrapper<>();
    submitWrapper.eq("userId", userId);
    submitWrapper.between("createTime", startDate, endDate);
    List<QuestionSubmit> submits = questionSubmitService.list(submitWrapper);

    // 创建学习报告
    LearningReport report = new LearningReport();
    report.setUserId(userId);
    report.setReportType(reportType);
    report.setStartDate(startDate);
    report.setEndDate(endDate);
    report.setCreateTime(new Date());

    // 统计数据
    int totalSubmits = submits.size();
    int passedQuestions =
        (int) submits.stream().filter(s -> s.getStatus() != null && s.getStatus() == 2).count();
    double accuracyRate = totalSubmits > 0 ? (passedQuestions * 100.0 / totalSubmits) : 0;

    report.setTotalSubmits(totalSubmits);
    report.setPassedQuestions(passedQuestions);
    report.setAccuracyRate(accuracyRate);

    // 估算学习时长（每次提交平均5分钟）
    int studyTime = totalSubmits * 5;
    report.setStudyTime(studyTime);

    // 分析语言偏好
    Map<String, Integer> languageStats = new HashMap<>();
    for (QuestionSubmit submit : submits) {
      String language = submit.getLanguage();
      if (language != null) {
        languageStats.put(language, languageStats.getOrDefault(language, 0) + 1);
      }
    }
    report.setLanguagePreference(JSONUtil.toJsonStr(languageStats));

    // 分析得分分布
    Map<String, Integer> scoreDistribution = new HashMap<>();
    scoreDistribution.put("优秀(90-100)", 0);
    scoreDistribution.put("良好(80-89)", 0);
    scoreDistribution.put("中等(70-79)", 0);
    scoreDistribution.put("及格(60-69)", 0);
    scoreDistribution.put("不及格(<60)", 0);

    for (QuestionSubmit submit : submits) {
      if (submit.getJudgeInfo() != null && submit.getJudgeInfo().contains("score")) {
        // 简化处理：如果有分数信息
        int score = extractScore(submit.getJudgeInfo());
        if (score >= 90) {
          scoreDistribution.put("优秀(90-100)", scoreDistribution.get("优秀(90-100)") + 1);
        } else if (score >= 80) {
          scoreDistribution.put("良好(80-89)", scoreDistribution.get("良好(80-89)") + 1);
        } else if (score >= 70) {
          scoreDistribution.put("中等(70-79)", scoreDistribution.get("中等(70-79)") + 1);
        } else if (score >= 60) {
          scoreDistribution.put("及格(60-69)", scoreDistribution.get("及格(60-69)") + 1);
        } else {
          scoreDistribution.put("不及格(<60)", scoreDistribution.get("不及格(<60)") + 1);
        }
      }
    }
    report.setScoreDistribution(JSONUtil.toJsonStr(scoreDistribution));

    // 生成学习总结和建议
    String summary = generateSummary(report);
    String suggestions = generateSuggestions(report);

    report.setSummary(summary);
    report.setSuggestions(suggestions);

    // 保存完整报告数据
    Map<String, Object> reportData = new HashMap<>();
    reportData.put("totalSubmits", totalSubmits);
    reportData.put("passedQuestions", passedQuestions);
    reportData.put("accuracyRate", accuracyRate);
    reportData.put("studyTime", studyTime);
    reportData.put("languageStats", languageStats);
    reportData.put("scoreDistribution", scoreDistribution);
    report.setReportData(JSONUtil.toJsonStr(reportData));

    this.save(report);
    return report.getId();
  }

  @Override
  @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
  public void generateDailyReports() {
    log.info("开始生成日报...");
    // 获取所有活跃用户
    QueryWrapper<User> userWrapper = new QueryWrapper<>();
    userWrapper.eq("userRole", "user");
    List<User> users = userService.list(userWrapper);

    for (User user : users) {
      try {
        LearningReportGenerateRequest request = new LearningReportGenerateRequest();
        request.setReportType("DAILY");
        generateReport(request, user.getId());
      } catch (Exception e) {
        log.error("为用户 {} 生成日报失败: {}", user.getId(), e.getMessage());
      }
    }
    log.info("日报生成完成");
  }

  @Override
  @Scheduled(cron = "0 0 2 ? * MON") // 每周一凌晨2点执行
  public void generateWeeklyReports() {
    log.info("开始生成周报...");
    QueryWrapper<User> userWrapper = new QueryWrapper<>();
    userWrapper.eq("userRole", "user");
    List<User> users = userService.list(userWrapper);

    for (User user : users) {
      try {
        LearningReportGenerateRequest request = new LearningReportGenerateRequest();
        request.setReportType("WEEKLY");
        generateReport(request, user.getId());
      } catch (Exception e) {
        log.error("为用户 {} 生成周报失败: {}", user.getId(), e.getMessage());
      }
    }
    log.info("周报生成完成");
  }

  @Override
  @Scheduled(cron = "0 0 3 1 * ?") // 每月1号凌晨3点执行
  public void generateMonthlyReports() {
    log.info("开始生成月报...");
    QueryWrapper<User> userWrapper = new QueryWrapper<>();
    userWrapper.eq("userRole", "user");
    List<User> users = userService.list(userWrapper);

    for (User user : users) {
      try {
        LearningReportGenerateRequest request = new LearningReportGenerateRequest();
        request.setReportType("MONTHLY");
        generateReport(request, user.getId());
      } catch (Exception e) {
        log.error("为用户 {} 生成月报失败: {}", user.getId(), e.getMessage());
      }
    }
    log.info("月报生成完成");
  }

  @Override
  public LearningReport getLatestReport(Long userId, String reportType) {
    if (userId == null || userId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    return this.baseMapper.getLatestReportByType(userId, reportType);
  }

  @Override
  public List<LearningReport> getRecentReports(Long userId, int limit) {
    if (userId == null || userId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    return this.baseMapper.getRecentReports(userId, limit);
  }

  @Override
  public List<LearningReport> getReportsByDateRange(Long userId, Date startDate, Date endDate) {
    if (userId == null || userId <= 0 || startDate == null || endDate == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    return this.baseMapper.getReportsByDateRange(userId, startDate, endDate);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean deleteReport(Long id, Long userId) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    LearningReport report = this.getById(id);
    if (report == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "报告不存在");
    }

    // 只能删除自己的报告
    if (!report.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除此报告");
    }

    return this.removeById(id);
  }

  @Override
  public String exportReport(Long id, String format) {
    // 预留接口，后续实现
    return "导出功能开发中，敬请期待";
  }

  /** 从判题信息中提取分数 */
  private int extractScore(String judgeInfo) {
    // 简化实现，实际需要根据具体格式解析
    try {
      if (judgeInfo.contains("\"score\":")) {
        int start = judgeInfo.indexOf("\"score\":") + 9;
        int end = judgeInfo.indexOf(",", start);
        if (end == -1) {
          end = judgeInfo.indexOf("}", start);
        }
        String scoreStr = judgeInfo.substring(start, end).trim();
        return Integer.parseInt(scoreStr);
      }
    } catch (Exception e) {
      log.warn("解析分数失败: {}", judgeInfo);
    }
    return 0;
  }

  /** 生成学习总结 */
  private String generateSummary(LearningReport report) {
    StringBuilder summary = new StringBuilder();

    summary.append("在").append(report.getReportType()).append("学习期间，");

    if (report.getTotalSubmits() > 0) {
      summary.append("您共提交了 ").append(report.getTotalSubmits()).append(" 次代码，");
      summary.append("成功通过了 ").append(report.getPassedQuestions()).append(" 道题目，");
      summary.append("正确率为 ").append(String.format("%.1f%%", report.getAccuracyRate())).append("。");
    } else {
      summary.append("您还没有提交记录。");
    }

    if (report.getStudyTime() > 0) {
      int hours = report.getStudyTime() / 60;
      int minutes = report.getStudyTime() % 60;
      if (hours > 0) {
        summary.append("学习时长约 ").append(hours).append(" 小时 ").append(minutes).append(" 分钟。");
      } else {
        summary.append("学习时长约 ").append(minutes).append(" 分钟。");
      }
    }

    return summary.toString();
  }

  /** 生成学习建议 */
  private String generateSuggestions(LearningReport report) {
    List<String> suggestions = new ArrayList<>();

    // 根据正确率给出建议
    if (report.getAccuracyRate() < 50) {
      suggestions.add("建议先从简单题目开始，巩固基础知识。");
    } else if (report.getAccuracyRate() < 80) {
      suggestions.add("正确率有待提高，建议多做练习，总结错误经验。");
    } else {
      suggestions.add("正确率不错，可以尝试更有挑战性的题目。");
    }

    // 根据提交量给出建议
    if (report.getTotalSubmits() < 10) {
      suggestions.add("练习量偏少，建议增加练习频率。");
    }

    // 根据语言偏好给出建议
    if (report.getLanguagePreference() != null) {
      suggestions.add("继续使用熟悉的编程语言，提高熟练度。");
    }

    return String.join("\n", suggestions);
  }
}
