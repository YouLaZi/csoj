package com.oj.cs.model.dto.questionsubmit;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

/** 创建请求 */
@Data
public class QuestionSubmitAddRequest implements Serializable {

  /** 编程语言 */
  @NotBlank(message = "编程语言不能为空")
  @Size(max = 20, message = "编程语言长度不能超过20个字符")
  private String language;

  /** 用户代码 */
  @NotBlank(message = "代码不能为空")
  @Size(max = 65535, message = "代码长度不能超过65535个字符")
  private String code;

  /** 题目 id */
  @NotNull(message = "题目ID不能为空")
  private Long questionId;

  private static final long serialVersionUID = 1L;
}
