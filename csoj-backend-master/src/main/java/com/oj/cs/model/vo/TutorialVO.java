package com.oj.cs.model.vo;

import java.util.List;

import lombok.Data;

/** 新手教程视图对象 */
@Data
public class TutorialVO {

  /** 教程ID */
  private Long id;

  /** 教程标题 */
  private String title;

  /** 教程描述 */
  private String description;

  /** 教程步骤列表 */
  private List<TutorialStep> steps;

  /** 是否已完成 */
  private Boolean completed;

  /** 教程步骤 */
  @Data
  public static class TutorialStep {
    /** 步骤序号 */
    private Integer order;

    /** 步骤标题 */
    private String title;

    /** 步骤内容（支持Markdown） */
    private String content;

    /** 代码示例（可选） */
    private String codeExample;

    /** 编程语言（如果有代码示例） */
    private String language;
  }
}
