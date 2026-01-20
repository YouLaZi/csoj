package com.oj.cs.model.enums;

import java.util.Arrays;

/** 导出类型枚举 */
public enum ExportTypeEnum {

  /** 比赛排名导出 */
  CONTEST_RANKING("contest_ranking", "比赛排名"),

  /** 用户成绩导出 */
  USER_SCORES("user_scores", "用户成绩"),

  /** 题目统计导出 */
  QUESTION_STATS("question_stats", "题目统计"),

  /** 判题记录导出 */
  SUBMIT_RECORDS("submit_records", "判题记录"),

  /** 学习报告导出 */
  LEARNING_REPORT("learning_report", "学习报告"),

  /** 代码查重报告导出 */
  SIMILARITY_REPORT("similarity_report", "代码查重报告");

  private final String code;
  private final String desc;

  ExportTypeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  public static ExportTypeEnum getByCode(String code) {
    return Arrays.stream(ExportTypeEnum.values())
        .filter(e -> e.getCode().equals(code))
        .findFirst()
        .orElse(null);
  }
}
