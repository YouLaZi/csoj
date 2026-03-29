package com.oj.cs.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.chat.ChatMessageRequest;
import com.oj.cs.model.dto.chat.ChatMessageResponse;
import com.oj.cs.model.entity.ChatMessage;

/** ChatBot Service Interface Supports both streaming (SSE) and non-streaming responses */
public interface ChatBotService extends IService<ChatMessage> {

  /**
   * Stream chat message response via SSE
   *
   * @param request Chat message request
   * @param userId Current user ID
   * @return SSE emitter for streaming response
   */
  SseEmitter streamChatMessage(ChatMessageRequest request, Long userId);

  /**
   * Send chat message (non-streaming fallback)
   *
   * @param request Chat message request
   * @param userId Current user ID
   * @return Chat message response
   */
  ChatMessageResponse sendChatMessage(ChatMessageRequest request, Long userId);

  /**
   * Get user learning progress
   *
   * @param userId User ID
   * @return Learning progress info
   */
  Map<String, Object> getLearningProgress(Long userId);

  /**
   * Get recommended problems for user
   *
   * @param userId User ID
   * @return List of recommended problems with metadata
   */
  List<Map<String, Object>> getRecommendedProblems(Long userId);

  /**
   * Save chat message to database
   *
   * @param chatMessage Chat message to save
   * @return Saved message ID
   */
  Long saveChatMessage(ChatMessage chatMessage);

  /**
   * Get chat history for user
   *
   * @param userId User ID
   * @param questionId Question ID (optional, filters by question if provided)
   * @return List of chat messages ordered by time
   */
  List<ChatMessage> getChatHistory(Long userId, Long questionId);

  /**
   * Clear chat history for user
   *
   * @param userId User ID
   * @param questionId Question ID (optional, clears all if null)
   */
  void clearChatHistory(Long userId, Long questionId);
}
