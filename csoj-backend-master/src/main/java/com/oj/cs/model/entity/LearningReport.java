package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 学习报告实体 */
@TableName(value = "learning_report")
@Data
public class LearningReport implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 用户ID */
  private Long userId;

  /** 报告类型（DAILY-日报, WEEKLY-周报, MONTHLY-月报, CUSTOM-自定义） */
  private String reportType;

  /** 开始日期 */
  private Date startDate;

  /** 结束日期 */
  private Date endDate;

  /** 总提交次数 */
  private Integer totalSubmits;

  /** 通过题目数 */
  private Integer passedQuestions;

  /** 正确率 */
  private Double accuracyRate;

  /** 学习时长（分钟） */
  private Integer studyTime;

  /** 掌握的知识点（JSON格式） */
  private String knowledgePoints;

  /** 薄弱知识点（JSON格式） */
  private String weakPoints;

  /** 得分分布（JSON格式） */
  private String scoreDistribution;

  /** 编程语言偏好（JSON格式） */
  private String languagePreference;

  /** 学习总结 */
  private String summary;

  /** 学习建议 */
  private String suggestions;

  /** 完整报告数据（JSON格式） */
  private String reportData;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 报告类型枚举 */
  public enum ReportType {
    /** 日报 */
    DAILY,
    /** 周报 */
    WEEKLY,
    /** 月报 */
    MONTHLY,
    /** 自定义 */
    CUSTOM
  }
}
