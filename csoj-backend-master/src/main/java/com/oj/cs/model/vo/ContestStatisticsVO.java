package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

/** 比赛统计视图对象 */
@Data
public class ContestStatisticsVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 比赛ID */
  private Long contestId;

  /** 比赛标题 */
  private String contestTitle;

  /** 参与人数 */
  private Integer participantCount;

  /** 实际提交人数 */
  private Integer submitterCount;

  /** 总提交次数 */
  private Integer totalSubmits;

  /** 题目统计列表 */
  private List<QuestionStatistics> questionStatisticsList;

  /** 题目统计 */
  @Data
  public static class QuestionStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;

    /** 题目标题 */
    private String questionTitle;

    /** 通过人数 */
    private Integer passedCount;

    /** 提交人数 */
    private Integer submitterCount;

    /** 总提交次数 */
    private Integer totalSubmits;

    /** 通过率 */
    private Double passRate;
  }

  /** 得分分布 */
  private Map<String, Integer> scoreDistribution;

  /** 平均分 */
  private Double averageScore;

  /** 最高分 */
  private Integer maxScore;

  /** 最低分 */
  private Integer minScore;

  /** 中位数 */
  private Integer medianScore;
}
