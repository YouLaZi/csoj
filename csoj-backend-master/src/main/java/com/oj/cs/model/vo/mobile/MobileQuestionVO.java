package com.oj.cs.model.vo.mobile;

import java.io.Serializable;

import lombok.Data;

/** 移动端题目视图对象 只包含移动端显示必需的字段，减少数据传输 */
@Data
public class MobileQuestionVO implements Serializable {

  /** 题目ID */
  private Long id;

  /** 题目标题 */
  private String title;

  /** 难度 (简单/中等/困难) */
  private String difficulty;

  /** 难度等级（用于排序和筛选） */
  private Integer difficultyLevel;

  /** 标签（数组格式，方便移动端渲染） */
  private String[] tags;

  /** 通过状态（当前用户是否已通过） */
  private Boolean passed;

  /** 提交数（简化显示） */
  private Integer submitNum;

  /** 通过率（百分比，如 85.5） */
  private Double passRate;

  /** 题目得分（用于比赛场景） */
  private Integer score;

  /** 缩略内容（用于列表展示，不包含完整题目描述） */
  private String summary;

  /** 题目类型（0=题库 1=比赛 2=作业） */
  private Integer questionType;

  /** 排序权重 */
  private Integer sortOrder;

  /** 是否收藏 */
  private Boolean favourited;

  private static final long serialVersionUID = 1L;
}
