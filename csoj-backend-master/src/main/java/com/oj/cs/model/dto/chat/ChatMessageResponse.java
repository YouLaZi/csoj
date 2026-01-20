package com.oj.cs.model.dto.chat;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 聊天消息响应 */
@Data
public class ChatMessageResponse implements Serializable {

  /** 回复内容 */
  private String content;

  /** 内容类型（text-文本，code-代码，math-数学公式） */
  private String contentType;

  /** 编程语言（当contentType为code时有效） */
  private String language;

  /** 代码分析结果（可选） */
  private CodeAnalysis codeAnalysis;

  @Data
  public static class CodeAnalysis {
    /** 错误列表 */
    private List<String> errors;

    /** 建议列表 */
    private List<String> suggestions;
  }

  private static final long serialVersionUID = 1L;
}
