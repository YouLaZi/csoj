package com.oj.cs.model.dto.question;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 批量创建题目响应 */
@Data
public class QuestionBatchAddResponse implements Serializable {

  /** 总题目数 */
  private Integer totalCount;

  /** 成功导入数 */
  private Integer successCount;

  /** 失败数 */
  private Integer failCount;

  /** 成功列表 */
  private List<QuestionImportResult> successList;

  /** 失败列表 */
  private List<QuestionImportResult> failList;

  /** 题目导入结果 */
  @Data
  public static class QuestionImportResult implements Serializable {
    /** 索引 */
    private Integer index;

    /** 题目标题 */
    private String title;

    /** 是否成功 */
    private Boolean success;

    /** 消息 */
    private String message;

    /** 题目ID（成功时） */
    private Long questionId;

    private static final long serialVersionUID = 1L;
  }

  private static final long serialVersionUID = 1L;
}
