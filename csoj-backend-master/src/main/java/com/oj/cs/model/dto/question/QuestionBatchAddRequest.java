package com.oj.cs.model.dto.question;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 批量创建题目请求 */
@Data
public class QuestionBatchAddRequest implements Serializable {

  /** 题目列表 */
  private List<QuestionAddRequest> questionList;

  private static final long serialVersionUID = 1L;
}
