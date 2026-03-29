package com.oj.cs.controller;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.chat.ChatMessageRequest;
import com.oj.cs.model.dto.chat.ChatMessageResponse;
import com.oj.cs.model.entity.ChatMessage;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.ChatBotService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** ChatBot Controller Provides REST API and SSE streaming endpoints */
@RestController
@RequestMapping("/")
@Slf4j
public class ChatBotController {

  @Resource private ChatBotService chatBotService;

  @Resource private UserService userService;

  /**
   * Stream chat message via SSE Supports real-time streaming response from AI
   *
   * @param request Chat message request
   * @param httpRequest HTTP request
   * @return SSE emitter
   */
  @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamChatMessageGet(
      @RequestParam String message,
      @RequestParam(required = false) Long questionId,
      HttpServletRequest httpRequest) {
    ChatMessageRequest request = new ChatMessageRequest();
    request.setMessage(message);
    request.setQuestionId(questionId);
    return streamChatMessage(request, httpRequest);
  }

  /** Stream chat message via SSE (POST) */
  @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamChatMessage(
      @RequestBody ChatMessageRequest chatMessageRequest, HttpServletRequest httpRequest) {
    if (chatMessageRequest == null
        || chatMessageRequest.getMessage() == null
        || chatMessageRequest.getMessage().trim().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "Message cannot be empty");
    }

    User loginUser = userService.getLoginUser(httpRequest);
    log.info("Streaming chat message from user: {}", loginUser.getId());

    return chatBotService.streamChatMessage(chatMessageRequest, loginUser.getId());
  }

  /**
   * Send chat message (non-streaming, fallback)
   *
   * @param chatMessageRequest Chat message request
   * @param httpRequest HTTP request
   * @return Chat message response
   */
  @PostMapping("/chat/message")
  public BaseResponse<ChatMessageResponse> sendChatMessage(
      @RequestBody ChatMessageRequest chatMessageRequest, HttpServletRequest httpRequest) {
    if (chatMessageRequest == null
        || chatMessageRequest.getMessage() == null
        || chatMessageRequest.getMessage().trim().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "Message cannot be empty");
    }

    User loginUser = userService.getLoginUser(httpRequest);
    ChatMessageResponse response =
        chatBotService.sendChatMessage(chatMessageRequest, loginUser.getId());
    return ResultUtils.success(response);
  }

  /**
   * Get learning progress
   *
   * @param httpRequest HTTP request
   * @return Learning progress info
   */
  @GetMapping("/user/learning-progress")
  public BaseResponse<Map<String, Object>> getLearningProgress(HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    Map<String, Object> learningProgress = chatBotService.getLearningProgress(loginUser.getId());
    return ResultUtils.success(learningProgress);
  }

  /**
   * Get recommended problems
   *
   * @param httpRequest HTTP request
   * @return Recommended problems list
   */
  @GetMapping("/problems/recommended")
  public BaseResponse<List<Map<String, Object>>> getRecommendedProblems(
      HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    List<Map<String, Object>> recommendedProblems =
        chatBotService.getRecommendedProblems(loginUser.getId());
    return ResultUtils.success(recommendedProblems);
  }

  /**
   * Get chat history
   *
   * @param questionId Question ID (optional)
   * @param httpRequest HTTP request
   * @return Chat history list
   */
  @GetMapping("/chat/history")
  public BaseResponse<List<ChatMessage>> getChatHistory(
      @RequestParam(required = false) Long questionId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    List<ChatMessage> chatHistory = chatBotService.getChatHistory(loginUser.getId(), questionId);
    return ResultUtils.success(chatHistory);
  }

  /**
   * Clear chat history
   *
   * @param questionId Question ID (optional, null to clear all)
   * @param httpRequest HTTP request
   * @return Success response
   */
  @DeleteMapping("/chat/clear")
  public BaseResponse<Void> clearChatHistory(
      @RequestParam(required = false) Long questionId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    chatBotService.clearChatHistory(loginUser.getId(), questionId);
    return ResultUtils.success(null);
  }
}
