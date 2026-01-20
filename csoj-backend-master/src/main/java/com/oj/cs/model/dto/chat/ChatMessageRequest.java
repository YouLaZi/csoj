package com.oj.cs.model.dto.chat;

import java.io.Serializable;

import lombok.Data;

/** 聊天消息请求 */
@Data
public class ChatMessageRequest implements Serializable {

  /** 用户消息内容 */
  private String message;

  /** 问题ID（可选） */
  private Long questionId;

  private static final long serialVersionUID = 1L;
}
