package com.oj.cs.service;

import java.util.List;
import java.util.Map;

import com.oj.cs.model.dto.chat.ChatMessageRequest;
import com.oj.cs.model.dto.chat.ChatMessageResponse;
import com.oj.cs.model.entity.ChatMessage;

/** 聊天机器人服务 */
public interface ChatBotService {

  /**
   * 发送聊天消息
   *
   * @param chatMessageRequest 聊天消息请求
   * @param loginUserId 当前登录用户ID
   * @return 聊天消息响应
   */
  ChatMessageResponse sendChatMessage(ChatMessageRequest chatMessageRequest, Long loginUserId);

  /**
   * 获取用户学习进度
   *
   * @param userId 用户ID
   * @return 学习进度信息
   */
  Map<String, Object> getLearningProgress(Long userId);

  /**
   * 获取推荐题目
   *
   * @param userId 用户ID
   * @return 推荐题目列表
   */
  List<Map<String, Object>> getRecommendedProblems(Long userId);

  /**
   * 保存聊天消息
   *
   * @param chatMessage 聊天消息
   * @return 消息ID
   */
  Long saveChatMessage(ChatMessage chatMessage);

  /**
   * 获取用户聊天历史
   *
   * @param userId 用户ID
   * @param questionId 问题ID（可选）
   * @return 聊天历史列表
   */
  List<ChatMessage> getChatHistory(Long userId, Long questionId);
}
