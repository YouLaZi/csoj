package com.oj.cs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.similarity.CodeSimilarityQueryRequest;
import com.oj.cs.model.entity.CodeSimilarity;

/** 代码相似度服务接口 */
public interface CodeSimilarityService extends IService<CodeSimilarity> {

  /**
   * 检测单个题目下所有提交的代码相似度
   *
   * @param questionId 题目ID
   * @param threshold 相似度阈值，低于此值的记录不保存
   * @return 检测到的相似记录数量
   */
  int detectSimilarityForQuestion(Long questionId, Integer threshold);

  /**
   * 比较两次提交的代码相似度
   *
   * @param submitId1 提交记录1 ID
   * @param submitId2 提交记录2 ID
   * @return 相似度记录
   */
  CodeSimilarity compareSubmits(Long submitId1, Long submitId2);

  /** 分页查询相似记录 */
  IPage<CodeSimilarity> listSimilarityByPage(CodeSimilarityQueryRequest queryRequest);

  /** 标记相似记录为已处理 */
  Boolean markAsProcessed(Long id, String processResult);

  /**
   * 获取题目相似度统计
   *
   * @param questionId 题目ID
   * @return 统计信息
   */
  SimilarityStatistics getSimilarityStatistics(Long questionId);

  /** 相似度统计信息 */
  class SimilarityStatistics {
    private Long questionId;
    private Integer totalSubmits;
    private Integer highSimilarityPairs; // 高度相似对数 (>=80%)
    private Integer mediumSimilarityPairs; // 中度相似对数 (50-79%)
    private Integer lowSimilarityPairs; // 低度相似对数 (<50%)
    private Double averageSimilarity; // 平均相似度
    private Integer suspiciousUsers; // 可疑用户数

    // Getters and Setters
    public Long getQuestionId() {
      return questionId;
    }

    public void setQuestionId(Long questionId) {
      this.questionId = questionId;
    }

    public Integer getTotalSubmits() {
      return totalSubmits;
    }

    public void setTotalSubmits(Integer totalSubmits) {
      this.totalSubmits = totalSubmits;
    }

    public Integer getHighSimilarityPairs() {
      return highSimilarityPairs;
    }

    public void setHighSimilarityPairs(Integer highSimilarityPairs) {
      this.highSimilarityPairs = highSimilarityPairs;
    }

    public Integer getMediumSimilarityPairs() {
      return mediumSimilarityPairs;
    }

    public void setMediumSimilarityPairs(Integer mediumSimilarityPairs) {
      this.mediumSimilarityPairs = mediumSimilarityPairs;
    }

    public Integer getLowSimilarityPairs() {
      return lowSimilarityPairs;
    }

    public void setLowSimilarityPairs(Integer lowSimilarityPairs) {
      this.lowSimilarityPairs = lowSimilarityPairs;
    }

    public Double getAverageSimilarity() {
      return averageSimilarity;
    }

    public void setAverageSimilarity(Double averageSimilarity) {
      this.averageSimilarity = averageSimilarity;
    }

    public Integer getSuspiciousUsers() {
      return suspiciousUsers;
    }

    public void setSuspiciousUsers(Integer suspiciousUsers) {
      this.suspiciousUsers = suspiciousUsers;
    }
  }
}
