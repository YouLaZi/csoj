package com.oj.cs.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.*;
import com.oj.cs.model.dto.export.DataExportRequest;
import com.oj.cs.model.entity.Contest;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.enums.ExportTypeEnum;
import com.oj.cs.model.enums.QuestionSubmitStatusEnum;
import com.oj.cs.service.ContestService;
import com.oj.cs.service.ExportService;
import com.oj.cs.service.QuestionService;
import com.oj.cs.service.UserService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/** 数据导出服务实现 */
@Service
@Slf4j
public class ExportServiceImpl extends ServiceImpl<ContestMapper, Contest>
    implements ExportService {

  @Resource private ContestMapper contestMapper;

  @Resource private QuestionSubmitMapper questionSubmitMapper;

  @Resource private QuestionMapper questionMapper;

  @Resource private UserMapper userMapper;

  @Resource private UserService userService;

  @Resource private ContestService contestService;

  @Resource private QuestionService questionService;

  @Override
  public void exportContestRanking(Long contestId, String format, HttpServletResponse response)
      throws IOException {
    // 查询比赛信息
    Contest contest = contestMapper.selectById(contestId);
    if (contest == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }

    // 查询比赛相关的提交记录
    QueryWrapper<QuestionSubmit> submitQuery = new QueryWrapper<>();
    submitQuery
        .eq("contestId", contestId)
        .eq("status", QuestionSubmitStatusEnum.SUCCEED.getValue())
        .orderByDesc("createTime");
    List<QuestionSubmit> submits = questionSubmitMapper.selectList(submitQuery);

    // 按用户统计得分
    List<ContestRankingData> rankingDataList = calculateContestRanking(submits);

    // 设置响应头
    String fileName =
        URLEncoder.encode(contest.getTitle() + "-排名", "UTF-8").replaceAll("\\+", "%20");
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 写入Excel
    EasyExcel.write(response.getOutputStream(), ContestRankingData.class)
        .sheet("比赛排名")
        .doWrite(rankingDataList);

    log.info("导出比赛排名成功, contestId={}, 导出记录数={}", contestId, rankingDataList.size());
  }

  @Override
  public void exportUserScores(Long userId, String format, HttpServletResponse response)
      throws IOException {
    User user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
    }

    // 查询用户所有提交记录
    QueryWrapper<QuestionSubmit> query = new QueryWrapper<>();
    query.eq("userId", userId);
    List<QuestionSubmit> submits = questionSubmitMapper.selectList(query);

    // 转换为导出数据
    List<UserScoreData> scoreDataList =
        submits.stream().map(this::convertToUserScoreData).collect(Collectors.toList());

    // 设置响应头
    String fileName =
        URLEncoder.encode(user.getUserName() + "-成绩单", "UTF-8").replaceAll("\\+", "%20");
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 写入Excel
    EasyExcel.write(response.getOutputStream(), UserScoreData.class)
        .sheet("成绩记录")
        .doWrite(scoreDataList);

    log.info("导出用户成绩成功, userId={}, 导出记录数={}", userId, scoreDataList.size());
  }

  @Override
  public void exportQuestionStats(DataExportRequest request, HttpServletResponse response)
      throws IOException {
    // 查询题目列表
    List<Question> questions;
    if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
      questions = questionMapper.selectBatchIds(request.getQuestionIds());
    } else {
      questions = questionMapper.selectList(new QueryWrapper<>());
    }

    // 统计每个题目的数据
    List<QuestionStatsData> statsDataList = new ArrayList<>();
    for (Question question : questions) {
      QuestionStatsData statsData = calculateQuestionStats(question);
      statsDataList.add(statsData);
    }

    // 设置响应头
    String fileName =
        URLEncoder.encode("题目统计-" + DateUtil.today(), "UTF-8").replaceAll("\\+", "%20");
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 写入Excel
    EasyExcel.write(response.getOutputStream(), QuestionStatsData.class)
        .sheet("题目统计")
        .doWrite(statsDataList);

    log.info("导出题目统计成功, 导出题目数={}", statsDataList.size());
  }

  @Override
  public void exportSubmitRecords(DataExportRequest request, HttpServletResponse response)
      throws IOException {
    QueryWrapper<QuestionSubmit> query = new QueryWrapper<>();

    if (request.getUserId() != null) {
      query.eq("userId", request.getUserId());
    }
    if (request.getContestId() != null) {
      query.eq("contestId", request.getContestId());
    }
    if (StrUtil.isNotBlank(request.getStartTime())) {
      query.ge("createTime", request.getStartTime());
    }
    if (StrUtil.isNotBlank(request.getEndTime())) {
      query.le("createTime", request.getEndTime());
    }

    query.orderByDesc("createTime");
    List<QuestionSubmit> submits = questionSubmitMapper.selectList(query);

    // 转换为导出数据
    List<SubmitRecordData> recordDataList =
        submits.stream().map(this::convertToSubmitRecordData).collect(Collectors.toList());

    // 设置响应头
    String fileName =
        URLEncoder.encode("判题记录-" + DateUtil.today(), "UTF-8").replaceAll("\\+", "%20");
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 写入Excel
    EasyExcel.write(response.getOutputStream(), SubmitRecordData.class)
        .sheet("判题记录")
        .doWrite(recordDataList);

    log.info("导出判题记录成功, 导出记录数={}", recordDataList.size());
  }

  @Override
  public void exportLearningReport(Long userId, String reportType, HttpServletResponse response)
      throws IOException {
    // 学习报告导出实现（可以从 LearningReportService 获取数据）
    User user = userMapper.selectById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
    }

    // 查询用户提交统计
    QueryWrapper<QuestionSubmit> query = new QueryWrapper<>();
    query.eq("userId", userId);
    List<QuestionSubmit> submits = questionSubmitMapper.selectList(query);

    // 生成学习报告数据
    LearningReportData reportData = generateLearningReportData(user, submits);

    // 设置响应头
    String fileName =
        URLEncoder.encode(user.getUserName() + "-学习报告", "UTF-8").replaceAll("\\+", "%20");
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 写入Excel
    List<LearningReportData> reportList = new ArrayList<>();
    reportList.add(reportData);
    EasyExcel.write(response.getOutputStream(), LearningReportData.class)
        .sheet("学习报告")
        .doWrite(reportList);

    log.info("导出学习报告成功, userId={}", userId);
  }

  @Override
  public void exportSimilarityReport(Long questionId, HttpServletResponse response)
      throws IOException {
    // 代码查重报告导出实现
    Question question = questionMapper.selectById(questionId);
    if (question == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
    }

    // 查询该题目的所有提交
    QueryWrapper<QuestionSubmit> query = new QueryWrapper<>();
    query.eq("questionId", questionId);
    List<QuestionSubmit> submits = questionSubmitMapper.selectList(query);

    // 设置响应头
    String fileName =
        URLEncoder.encode("查重报告-" + question.getTitle(), "UTF-8").replaceAll("\\+", "%20");
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 写入Excel（简化实现，实际应从 CodeSimilarity 表获取查重结果）
    List<SimilarityReportData> reportDataList =
        submits.stream().map(this::convertToSimilarityReportData).collect(Collectors.toList());

    EasyExcel.write(response.getOutputStream(), SimilarityReportData.class)
        .sheet("查重报告")
        .doWrite(reportDataList);

    log.info("导出查重报告成功, questionId={}", questionId);
  }

  @Override
  public void exportData(DataExportRequest request, HttpServletResponse response)
      throws IOException {
    ExportTypeEnum exportType = ExportTypeEnum.getByCode(request.getExportType());
    if (exportType == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的导出类型");
    }

    String format = request.getFormat();
    if (StrUtil.isBlank(format)) {
      format = "excel";
    }

    switch (exportType) {
      case CONTEST_RANKING:
        exportContestRanking(request.getContestId(), format, response);
        break;
      case USER_SCORES:
        exportUserScores(request.getUserId(), format, response);
        break;
      case QUESTION_STATS:
        exportQuestionStats(request, response);
        break;
      case SUBMIT_RECORDS:
        exportSubmitRecords(request, response);
        break;
      case LEARNING_REPORT:
        exportLearningReport(request.getUserId(), "daily", response);
        break;
      case SIMILARITY_REPORT:
        // TODO: 实现相似度报告导出
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "相似度报告导出待实现");
        // exportSimilarityReport(request.getQuestionIds(), response);
      default:
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的导出类型");
    }
  }

  /** 计算比赛排名 */
  private List<ContestRankingData> calculateContestRanking(List<QuestionSubmit> submits) {
    // 按用户分组，计算每个用户的得分
    List<ContestRankingData> rankingList = new ArrayList<>();

    // 简化实现：按用户统计通过题目数和总得分
    submits.stream()
        .collect(Collectors.groupingBy(QuestionSubmit::getUserId))
        .forEach(
            (userId, userSubmits) -> {
              User user = userMapper.selectById(userId);
              if (user != null) {
                ContestRankingData data = new ContestRankingData();
                data.setUserId(userId);
                data.setUserName(user.getUserName());
                data.setRealName(user.getUserRole());
                data.setSolvedCount(
                    (int)
                        userSubmits.stream()
                            .filter(
                                s ->
                                    s.getStatus()
                                        .equals(QuestionSubmitStatusEnum.SUCCEED.getValue()))
                            .count());
                data.setTotalScore(
                    userSubmits.stream()
                        .mapToInt(
                            s -> {
                              // 从 judgeInfo JSON 中提取分数，默认为 0
                              try {
                                String judgeInfo = s.getJudgeInfo();
                                if (judgeInfo != null && judgeInfo.startsWith("{")) {
                                  // 简单解析 JSON 提取 score 字段
                                  if (judgeInfo.contains("\"score\":")) {
                                    int scoreIdx = judgeInfo.indexOf("\"score\":");
                                    int colonIdx = judgeInfo.indexOf(":", scoreIdx);
                                    int endIdx = judgeInfo.indexOf(",", colonIdx);
                                    if (endIdx == -1) {
                                      endIdx = judgeInfo.indexOf("}", colonIdx);
                                    }
                                    String scoreStr =
                                        judgeInfo.substring(colonIdx + 1, endIdx).trim();
                                    return Integer.parseInt(scoreStr);
                                  }
                                }
                                return 0;
                              } catch (Exception e) {
                                return 0;
                              }
                            })
                        .sum());
                data.setSubmitCount(userSubmits.size());
                rankingList.add(data);
              }
            });

    // 按得分排序
    rankingList.sort(Comparator.comparingInt(ContestRankingData::getTotalScore).reversed());

    // 添加排名
    for (int i = 0; i < rankingList.size(); i++) {
      rankingList.get(i).setRank(i + 1);
    }

    return rankingList;
  }

  /** 计算题目统计 */
  private QuestionStatsData calculateQuestionStats(Question question) {
    QuestionStatsData data = new QuestionStatsData();
    data.setQuestionId(question.getId());
    data.setTitle(question.getTitle());
    data.setDifficulty(question.getDifficulty());
    data.setTags(question.getTags());

    // 查询该题目的提交记录
    QueryWrapper<QuestionSubmit> query = new QueryWrapper<>();
    query.eq("questionId", question.getId());
    List<QuestionSubmit> submits = questionSubmitMapper.selectList(query);

    long totalCount = submits.size();
    long successCount =
        submits.stream()
            .filter(s -> s.getStatus().equals(QuestionSubmitStatusEnum.SUCCEED.getValue()))
            .count();

    data.setSubmitCount((int) totalCount);
    data.setSuccessCount((int) successCount);
    data.setSuccessRate(totalCount > 0 ? (successCount * 100.0 / totalCount) : 0);

    return data;
  }

  /** 生成学习报告数据 */
  private LearningReportData generateLearningReportData(User user, List<QuestionSubmit> submits) {
    LearningReportData data = new LearningReportData();
    data.setUserId(user.getId());
    data.setUserName(user.getUserName());
    data.setTotalSubmit(submits.size());
    data.setSuccessSubmit(
        (int)
            submits.stream()
                .filter(s -> s.getStatus().equals(QuestionSubmitStatusEnum.SUCCEED.getValue()))
                .count());
    data.setSuccessRate(
        data.getTotalSubmit() > 0 ? (data.getSuccessSubmit() * 100.0 / data.getTotalSubmit()) : 0);
    data.setReportDate(DateUtil.today());

    return data;
  }

  private UserScoreData convertToUserScoreData(QuestionSubmit submit) {
    UserScoreData data = new UserScoreData();
    data.setQuestionId(submit.getQuestionId());
    data.setLanguage(submit.getLanguage());
    data.setStatus(submit.getStatus());
    // 从 JSON 字符串解析分数
    data.setJudgeInfo(parseScoreFromJudgeInfo(submit.getJudgeInfo()));
    // 格式化日期为字符串
    data.setCreateTime(formatDate(submit.getCreateTime()));
    return data;
  }

  private SubmitRecordData convertToSubmitRecordData(QuestionSubmit submit) {
    SubmitRecordData data = new SubmitRecordData();
    data.setId(submit.getId());
    data.setUserId(submit.getUserId());
    data.setQuestionId(submit.getQuestionId());
    data.setLanguage(submit.getLanguage());
    data.setStatus(submit.getStatus());
    // 从 JSON 字符串解析分数
    data.setJudgeInfo(parseScoreFromJudgeInfo(submit.getJudgeInfo()));
    // 格式化日期为字符串
    data.setCreateTime(formatDate(submit.getCreateTime()));
    return data;
  }

  /** 从 judgeInfo JSON 中提取分数 */
  private Integer parseScoreFromJudgeInfo(String judgeInfo) {
    if (judgeInfo == null || !judgeInfo.startsWith("{")) {
      return 0;
    }
    try {
      if (judgeInfo.contains("\"score\":")) {
        int scoreIdx = judgeInfo.indexOf("\"score\":");
        int colonIdx = judgeInfo.indexOf(":", scoreIdx);
        int endIdx = judgeInfo.indexOf(",", colonIdx);
        if (endIdx == -1) {
          endIdx = judgeInfo.indexOf("}", colonIdx);
        }
        String scoreStr = judgeInfo.substring(colonIdx + 1, endIdx).trim();
        return Integer.parseInt(scoreStr);
      }
    } catch (Exception e) {
      // 忽略解析错误
    }
    return 0;
  }

  /** 格式化日期为字符串 */
  private String formatDate(java.util.Date date) {
    if (date == null) {
      return "";
    }
    return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
  }

  private SimilarityReportData convertToSimilarityReportData(QuestionSubmit submit) {
    SimilarityReportData data = new SimilarityReportData();
    data.setSubmitId(submit.getId());
    data.setUserId(submit.getUserId());
    data.setQuestionId(submit.getQuestionId());
    data.setLanguage(submit.getLanguage());
    data.setCreateTime(formatDate(submit.getCreateTime()));
    data.setSimilarityScore(0.0); // 实际应从 CodeSimilarity 表获取
    return data;
  }

  /** 比赛排名数据 */
  @com.alibaba.excel.annotation.ExcelIgnoreUnannotated
  public static class ContestRankingData {
    @com.alibaba.excel.annotation.ExcelProperty("排名")
    private Integer rank;

    @com.alibaba.excel.annotation.ExcelProperty("用户ID")
    private Long userId;

    @com.alibaba.excel.annotation.ExcelProperty("用户名")
    private String userName;

    @com.alibaba.excel.annotation.ExcelProperty("真实姓名")
    private String realName;

    @com.alibaba.excel.annotation.ExcelProperty("通过题数")
    private Integer solvedCount;

    @com.alibaba.excel.annotation.ExcelProperty("总得分")
    private Integer totalScore;

    @com.alibaba.excel.annotation.ExcelProperty("提交次数")
    private Integer submitCount;

    public Integer getRank() {
      return rank;
    }

    public void setRank(Integer rank) {
      this.rank = rank;
    }

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public String getRealName() {
      return realName;
    }

    public void setRealName(String realName) {
      this.realName = realName;
    }

    public Integer getSolvedCount() {
      return solvedCount;
    }

    public void setSolvedCount(Integer solvedCount) {
      this.solvedCount = solvedCount;
    }

    public Integer getTotalScore() {
      return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
      this.totalScore = totalScore;
    }

    public Integer getSubmitCount() {
      return submitCount;
    }

    public void setSubmitCount(Integer submitCount) {
      this.submitCount = submitCount;
    }
  }

  /** 用户成绩数据 */
  @com.alibaba.excel.annotation.ExcelIgnoreUnannotated
  public static class UserScoreData {
    @com.alibaba.excel.annotation.ExcelProperty("题目ID")
    private Long questionId;

    @com.alibaba.excel.annotation.ExcelProperty("编程语言")
    private String language;

    @com.alibaba.excel.annotation.ExcelProperty("判题状态")
    private Integer status;

    @com.alibaba.excel.annotation.ExcelProperty("得分")
    private Integer judgeInfo;

    @com.alibaba.excel.annotation.ExcelProperty("提交时间")
    private String createTime;

    public Long getQuestionId() {
      return questionId;
    }

    public void setQuestionId(Long questionId) {
      this.questionId = questionId;
    }

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    public Integer getStatus() {
      return status;
    }

    public void setStatus(Integer status) {
      this.status = status;
    }

    public Integer getJudgeInfo() {
      return judgeInfo;
    }

    public void setJudgeInfo(Integer judgeInfo) {
      this.judgeInfo = judgeInfo;
    }

    public String getCreateTime() {
      return createTime;
    }

    public void setCreateTime(String createTime) {
      this.createTime = createTime;
    }
  }

  /** 题目统计数据 */
  @com.alibaba.excel.annotation.ExcelIgnoreUnannotated
  public static class QuestionStatsData {
    @com.alibaba.excel.annotation.ExcelProperty("题目ID")
    private Long questionId;

    @com.alibaba.excel.annotation.ExcelProperty("题目标题")
    private String title;

    @com.alibaba.excel.annotation.ExcelProperty("难度")
    private String difficulty;

    @com.alibaba.excel.annotation.ExcelProperty("标签")
    private String tags;

    @com.alibaba.excel.annotation.ExcelProperty("提交次数")
    private Integer submitCount;

    @com.alibaba.excel.annotation.ExcelProperty("通过次数")
    private Integer successCount;

    @com.alibaba.excel.annotation.ExcelProperty("通过率")
    private Double successRate;

    public Long getQuestionId() {
      return questionId;
    }

    public void setQuestionId(Long questionId) {
      this.questionId = questionId;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getDifficulty() {
      return difficulty;
    }

    public void setDifficulty(String difficulty) {
      this.difficulty = difficulty;
    }

    public String getTags() {
      return tags;
    }

    public void setTags(String tags) {
      this.tags = tags;
    }

    public Integer getSubmitCount() {
      return submitCount;
    }

    public void setSubmitCount(Integer submitCount) {
      this.submitCount = submitCount;
    }

    public Integer getSuccessCount() {
      return successCount;
    }

    public void setSuccessCount(Integer successCount) {
      this.successCount = successCount;
    }

    public Double getSuccessRate() {
      return successRate;
    }

    public void setSuccessRate(Double successRate) {
      this.successRate = successRate;
    }
  }

  /** 判题记录数据 */
  @com.alibaba.excel.annotation.ExcelIgnoreUnannotated
  public static class SubmitRecordData {
    @com.alibaba.excel.annotation.ExcelProperty("记录ID")
    private Long id;

    @com.alibaba.excel.annotation.ExcelProperty("用户ID")
    private Long userId;

    @com.alibaba.excel.annotation.ExcelProperty("题目ID")
    private Long questionId;

    @com.alibaba.excel.annotation.ExcelProperty("编程语言")
    private String language;

    @com.alibaba.excel.annotation.ExcelProperty("状态")
    private Integer status;

    @com.alibaba.excel.annotation.ExcelProperty("得分")
    private Integer judgeInfo;

    @com.alibaba.excel.annotation.ExcelProperty("创建时间")
    private String createTime;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public Long getQuestionId() {
      return questionId;
    }

    public void setQuestionId(Long questionId) {
      this.questionId = questionId;
    }

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    public Integer getStatus() {
      return status;
    }

    public void setStatus(Integer status) {
      this.status = status;
    }

    public Integer getJudgeInfo() {
      return judgeInfo;
    }

    public void setJudgeInfo(Integer judgeInfo) {
      this.judgeInfo = judgeInfo;
    }

    public String getCreateTime() {
      return createTime;
    }

    public void setCreateTime(String createTime) {
      this.createTime = createTime;
    }
  }

  /** 学习报告数据 */
  @com.alibaba.excel.annotation.ExcelIgnoreUnannotated
  public static class LearningReportData {
    @com.alibaba.excel.annotation.ExcelProperty("用户ID")
    private Long userId;

    @com.alibaba.excel.annotation.ExcelProperty("用户名")
    private String userName;

    @com.alibaba.excel.annotation.ExcelProperty("总提交数")
    private Integer totalSubmit;

    @com.alibaba.excel.annotation.ExcelProperty("成功提交数")
    private Integer successSubmit;

    @com.alibaba.excel.annotation.ExcelProperty("成功率")
    private Double successRate;

    @com.alibaba.excel.annotation.ExcelProperty("报告日期")
    private String reportDate;

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public Integer getTotalSubmit() {
      return totalSubmit;
    }

    public void setTotalSubmit(Integer totalSubmit) {
      this.totalSubmit = totalSubmit;
    }

    public Integer getSuccessSubmit() {
      return successSubmit;
    }

    public void setSuccessSubmit(Integer successSubmit) {
      this.successSubmit = successSubmit;
    }

    public Double getSuccessRate() {
      return successRate;
    }

    public void setSuccessRate(Double successRate) {
      this.successRate = successRate;
    }

    public String getReportDate() {
      return reportDate;
    }

    public void setReportDate(String reportDate) {
      this.reportDate = reportDate;
    }
  }

  /** 查重报告数据 */
  @com.alibaba.excel.annotation.ExcelIgnoreUnannotated
  public static class SimilarityReportData {
    @com.alibaba.excel.annotation.ExcelProperty("提交ID")
    private Long submitId;

    @com.alibaba.excel.annotation.ExcelProperty("用户ID")
    private Long userId;

    @com.alibaba.excel.annotation.ExcelProperty("题目ID")
    private Long questionId;

    @com.alibaba.excel.annotation.ExcelProperty("编程语言")
    private String language;

    @com.alibaba.excel.annotation.ExcelProperty("相似度分数")
    private Double similarityScore;

    @com.alibaba.excel.annotation.ExcelProperty("创建时间")
    private String createTime;

    public Long getSubmitId() {
      return submitId;
    }

    public void setSubmitId(Long submitId) {
      this.submitId = submitId;
    }

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public Long getQuestionId() {
      return questionId;
    }

    public void setQuestionId(Long questionId) {
      this.questionId = questionId;
    }

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    public Double getSimilarityScore() {
      return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
      this.similarityScore = similarityScore;
    }

    public String getCreateTime() {
      return createTime;
    }

    public void setCreateTime(String createTime) {
      this.createTime = createTime;
    }
  }
}
