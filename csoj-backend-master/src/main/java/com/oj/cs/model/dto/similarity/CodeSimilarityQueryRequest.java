package com.oj.cs.model.dto.similarity;

import java.io.Serializable;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 代码相似度查询请求 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodeSimilarityQueryRequest extends PageRequest implements Serializable {

  /** 题目ID */
  private Long questionId;

  /** 用户ID（查询该用户涉及的所有相似记录） */
  private Long userId;

  /** 最小相似度分数 */
  private Integer minScore;

  /** 相似度等级 */
  private String similarityLevel;

  /** 是否已处理 */
  private Integer isProcessed;

  /** 排序字段 */
  private String sortField;

  /** 排序方向 */
  private String sortOrder;

  private static final long serialVersionUID = 1L;
}
