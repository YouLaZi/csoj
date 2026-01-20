package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 比赛排行榜视图对象 */
@Data
public class ContestRankingVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 排名 */
  private Integer rank;

  /** 用户ID */
  private Long userId;

  /** 用户姓名 */
  private String userName;

  /** 用户账号 */
  private String userAccount;

  /** 总得分 */
  private Integer totalScore;

  /** 通过题目数 */
  private Integer passedCount;

  /** 总用时（秒） */
  private Long totalTime;

  /** 各题提交信息（题目ID -> 提交信息） */
  private List<QuestionSubmitInfo> submitInfos;

  /** 题目提交信息 */
  @Data
  public static class QuestionSubmitInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;

    /** 是否通过 */
    private Boolean passed;

    /** 提交次数 */
    private Integer submitCount;

    /** 用时（秒） */
    private Long timeUsed;
  }
}
