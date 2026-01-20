package com.oj.cs.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.CodeSimilarityMapper;
import com.oj.cs.model.dto.similarity.CodeSimilarityQueryRequest;
import com.oj.cs.model.entity.CodeSimilarity;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.service.CodeSimilarityService;
import com.oj.cs.service.QuestionSubmitService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码相似度服务实现 支持多种查重算法： 1. LCS (最长公共子序列) - 适合检测代码结构相似 2. Levenshtein 距离 - 适合检测编辑相似度 3. Jaccard 相似度 -
 * 适合检测 Token 集合相似度 4. AST 树相似度 - 适合检测语法树相似度（高级）
 */
@Service
@Slf4j
public class CodeSimilarityServiceImpl extends ServiceImpl<CodeSimilarityMapper, CodeSimilarity>
    implements CodeSimilarityService {

  @Resource private QuestionSubmitService questionSubmitService;

  private static final int SIMILARITY_THRESHOLD_HIGH = 80;
  private static final int SIMILARITY_THRESHOLD_MEDIUM = 50;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public int detectSimilarityForQuestion(Long questionId, Integer threshold) {
    if (questionId == null || questionId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 获取该题目的所有成功提交
    QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("questionId", questionId);
    queryWrapper.eq("status", 2); // 只检测成功的提交
    List<QuestionSubmit> submits = questionSubmitService.list(queryWrapper);

    if (submits.size() < 2) {
      log.info("题目 {} 的提交数量少于2，无需检测相似度", questionId);
      return 0;
    }

    int detectedCount = 0;
    threshold = threshold != null ? threshold : SIMILARITY_THRESHOLD_MEDIUM;

    // 两两比较提交代码
    for (int i = 0; i < submits.size(); i++) {
      for (int j = i + 1; j < submits.size(); j++) {
        QuestionSubmit submit1 = submits.get(i);
        QuestionSubmit submit2 = submits.get(j);

        // 跳过同一用户的提交
        if (submit1.getUserId().equals(submit2.getUserId())) {
          continue;
        }

        CodeSimilarity similarity = compareSubmits(submit1.getId(), submit2.getId());

        // 只保存相似度达到阈值的记录
        if (similarity.getSimilarityScore() >= threshold) {
          this.save(similarity);
          detectedCount++;
        }
      }
    }

    log.info("题目 {} 相似度检测完成，检测到 {} 对相似代码", questionId, detectedCount);
    return detectedCount;
  }

  @Override
  public CodeSimilarity compareSubmits(Long submitId1, Long submitId2) {
    if (submitId1 == null || submitId2 == null || submitId1.equals(submitId2)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    QuestionSubmit submit1 = questionSubmitService.getById(submitId1);
    QuestionSubmit submit2 = questionSubmitService.getById(submitId2);

    if (submit1 == null || submit2 == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
    }

    String code1 = submit1.getCode();
    String code2 = submit2.getCode();

    if (StrUtil.isBlank(code1) || StrUtil.isBlank(code2)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码为空");
    }

    // 预处理代码
    String normalizedCode1 = normalizeCode(code1);
    String normalizedCode2 = normalizeCode(code2);

    // 计算多种相似度
    double lcsSimilarity = calculateLCSSimilarity(normalizedCode1, normalizedCode2);
    double levenshteinSimilarity = calculateLevenshteinSimilarity(normalizedCode1, normalizedCode2);
    double jaccardSimilarity = calculateJaccardSimilarity(normalizedCode1, normalizedCode2);

    // 综合相似度（加权平均）
    double finalSimilarity =
        (lcsSimilarity * 0.4 + levenshteinSimilarity * 0.3 + jaccardSimilarity * 0.3);

    // 创建相似度记录
    CodeSimilarity similarity = new CodeSimilarity();
    similarity.setQuestionId(submit1.getQuestionId());
    similarity.setSubmitId1(submitId1);
    similarity.setUserId1(submit1.getUserId());
    similarity.setSubmitId2(submitId2);
    similarity.setUserId2(submit2.getUserId());
    similarity.setSimilarityScore((int) Math.round(finalSimilarity));
    similarity.setSimilarityLevel(getSimilarityLevel((int) Math.round(finalSimilarity)));
    similarity.setAlgorithmType("HYBRID");

    // 计算相似行数和总行数
    int totalLines = Math.max(code1.split("\n").length, code2.split("\n").length);
    similarity.setTotalLines(totalLines);
    similarity.setSimilarLines((int) (totalLines * finalSimilarity / 100));

    // 生成相似片段
    List<String> similarFragments = findSimilarFragments(normalizedCode1, normalizedCode2);
    similarity.setSimilarFragments(JSONUtil.toJsonStr(similarFragments));

    return similarity;
  }

  @Override
  public IPage<CodeSimilarity> listSimilarityByPage(CodeSimilarityQueryRequest queryRequest) {
    long current = queryRequest.getCurrent();
    long size = queryRequest.getPageSize();
    Page<CodeSimilarity> page = new Page<>(current, size);

    QueryWrapper<CodeSimilarity> queryWrapper = new QueryWrapper<>();

    if (queryRequest.getQuestionId() != null) {
      queryWrapper.eq("questionId", queryRequest.getQuestionId());
    }
    if (queryRequest.getUserId() != null) {
      queryWrapper.and(
          wrapper ->
              wrapper
                  .eq("user_id1", queryRequest.getUserId())
                  .or()
                  .eq("user_id2", queryRequest.getUserId()));
    }
    if (queryRequest.getMinScore() != null) {
      queryWrapper.ge("similarityScore", queryRequest.getMinScore());
    }
    if (StrUtil.isNotBlank(queryRequest.getSimilarityLevel())) {
      queryWrapper.eq("similarityLevel", queryRequest.getSimilarityLevel());
    }
    if (queryRequest.getIsProcessed() != null) {
      queryWrapper.eq("isProcessed", queryRequest.getIsProcessed());
    }

    // 排序
    String sortField =
        StrUtil.isNotBlank(queryRequest.getSortField())
            ? queryRequest.getSortField()
            : "createTime";
    String sortOrder =
        StrUtil.isNotBlank(queryRequest.getSortOrder()) ? queryRequest.getSortOrder() : "desc";

    if ("asc".equalsIgnoreCase(sortOrder)) {
      queryWrapper.orderByAsc(sortField);
    } else {
      queryWrapper.orderByDesc(sortField);
    }

    return this.page(page, queryWrapper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean markAsProcessed(Long id, String processResult) {
    CodeSimilarity similarity = this.getById(id);
    if (similarity == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }

    similarity.setIsProcessed(1);
    similarity.setProcessResult(processResult);

    return this.updateById(similarity);
  }

  @Override
  public SimilarityStatistics getSimilarityStatistics(Long questionId) {
    SimilarityStatistics statistics = new SimilarityStatistics();
    statistics.setQuestionId(questionId);

    // 获取该题目的所有提交数
    QueryWrapper<QuestionSubmit> submitWrapper = new QueryWrapper<>();
    submitWrapper.eq("questionId", questionId);
    long totalSubmits = questionSubmitService.count(submitWrapper);
    statistics.setTotalSubmits((int) totalSubmits);

    // 获取所有相似记录
    List<CodeSimilarity> similarities = this.baseMapper.getHighSimilarityByQuestion(questionId, 0);

    // 统计各级相似度数量
    int highCount = 0;
    int mediumCount = 0;
    int lowCount = 0;
    double totalSimilarity = 0;
    Set<Long> suspiciousUsers = new HashSet<>();

    for (CodeSimilarity similarity : similarities) {
      totalSimilarity += similarity.getSimilarityScore();
      suspiciousUsers.add(similarity.getUserId1());
      suspiciousUsers.add(similarity.getUserId2());

      if (similarity.getSimilarityScore() >= SIMILARITY_THRESHOLD_HIGH) {
        highCount++;
      } else if (similarity.getSimilarityScore() >= SIMILARITY_THRESHOLD_MEDIUM) {
        mediumCount++;
      } else {
        lowCount++;
      }
    }

    statistics.setHighSimilarityPairs(highCount);
    statistics.setMediumSimilarityPairs(mediumCount);
    statistics.setLowSimilarityPairs(lowCount);
    statistics.setAverageSimilarity(
        similarities.isEmpty() ? 0 : totalSimilarity / similarities.size());
    statistics.setSuspiciousUsers(suspiciousUsers.size());

    return statistics;
  }

  /** 预处理代码：移除注释、空行、统一空白字符 */
  private String normalizeCode(String code) {
    if (StrUtil.isBlank(code)) {
      return "";
    }

    // 移除单行注释
    code = code.replaceAll("//.*", "");
    // 移除多行注释
    code = code.replaceAll("/\\*.*?\\*/", "");
    // 移除空行
    code = code.replaceAll("\\s+\\n", "\n");
    // 统一空白字符为单个空格
    code = code.replaceAll("\\s+", " ");
    // 移除首尾空白
    code = code.trim();

    return code;
  }

  /** 计算 LCS 相似度（最长公共子序列） */
  private double calculateLCSSimilarity(String code1, String code2) {
    int lcsLength = longestCommonSubsequence(code1, code2);
    int maxLength = Math.max(code1.length(), code2.length());

    if (maxLength == 0) {
      return 0;
    }

    return (lcsLength * 100.0) / maxLength;
  }

  /** 计算最长公共子序列长度 */
  private int longestCommonSubsequence(String text1, String text2) {
    int m = text1.length();
    int n = text2.length();
    int[][] dp = new int[m + 1][n + 1];

    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1] + 1;
        } else {
          dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
        }
      }
    }

    return dp[m][n];
  }

  /** 计算 Levenshtein 相似度 */
  private double calculateLevenshteinSimilarity(String code1, String code2) {
    int distance = levenshteinDistance(code1, code2);
    int maxLength = Math.max(code1.length(), code2.length());

    if (maxLength == 0) {
      return 100;
    }

    return ((maxLength - distance) * 100.0) / maxLength;
  }

  /** 计算 Levenshtein 距离 */
  private int levenshteinDistance(String text1, String text2) {
    int m = text1.length();
    int n = text2.length();
    int[][] dp = new int[m + 1][n + 1];

    for (int i = 0; i <= m; i++) {
      dp[i][0] = i;
    }
    for (int j = 0; j <= n; j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
        }
      }
    }

    return dp[m][n];
  }

  /** 计算 Jaccard 相似度 */
  private double calculateJaccardSimilarity(String code1, String code2) {
    Set<String> set1 = tokenize(code1);
    Set<String> set2 = tokenize(code2);

    if (set1.isEmpty() && set2.isEmpty()) {
      return 100;
    }

    // 计算交集和并集
    Set<String> intersection = new HashSet<>(set1);
    intersection.retainAll(set2);

    Set<String> union = new HashSet<>(set1);
    union.addAll(set2);

    if (union.isEmpty()) {
      return 0;
    }

    return (intersection.size() * 100.0) / union.size();
  }

  /** 将代码分割为 Token 集合 */
  private Set<String> tokenize(String code) {
    // 简单的分词：按空格和常见符号分割
    String[] tokens = code.split("[\\s\\{\\}\\(\\)\\[\\]\\;\\,\\=\\+\\-\\*\\/\\<\\>]");
    return Arrays.stream(tokens).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
  }

  /** 查找相似代码片段 */
  private List<String> findSimilarFragments(String code1, String code2) {
    List<String> fragments = new ArrayList<>();

    // 简单实现：查找长度至少为20的公共子串
    int minLength = 20;
    String[] lines1 = code1.split("(?<=[.!?;])\\s+");
    String[] lines2 = code2.split("(?<=[.!?;])\\s+");

    for (String line1 : lines1) {
      if (line1.length() < minLength) continue;
      for (String line2 : lines2) {
        if (line2.length() < minLength) continue;

        double similarity = calculateLCSSimilarity(line1, line2);
        if (similarity >= 80) {
          fragments.add(line1.substring(0, Math.min(50, line1.length())) + "...");
          break;
        }
      }
    }

    return fragments;
  }

  /** 根据相似度分数获取等级 */
  private String getSimilarityLevel(int score) {
    if (score >= SIMILARITY_THRESHOLD_HIGH) {
      return "HIGH";
    } else if (score >= SIMILARITY_THRESHOLD_MEDIUM) {
      return "MEDIUM";
    } else {
      return "LOW";
    }
  }
}
