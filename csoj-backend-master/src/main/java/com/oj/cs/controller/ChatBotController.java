package com.oj.cs.controller;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

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

/** 聊天机器人接口 */
@RestController
@RequestMapping("/")
@Slf4j
public class ChatBotController {

  @Resource private ChatBotService chatBotService;

  @Resource private UserService userService;

  /**
   * 发送聊天消息
   *
   * @param chatMessageRequest 聊天消息请求
   * @param request HTTP请求
   * @return 聊天消息响应
   */
  @PostMapping("/chat/message")
  public BaseResponse<ChatMessageResponse> sendChatMessage(
      @RequestBody ChatMessageRequest chatMessageRequest, HttpServletRequest request) {
    if (chatMessageRequest == null || chatMessageRequest.getMessage() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
    }
    User loginUser = userService.getLoginUser(request);
    ChatMessageResponse response =
        chatBotService.sendChatMessage(chatMessageRequest, loginUser.getId());
    return ResultUtils.success(response);
  }

  /**
   * 获取学习进度
   *
   * @param request HTTP请求
   * @return 学习进度信息
   */
  @GetMapping("/user/learning-progress")
  public BaseResponse<Map<String, Object>> getLearningProgress(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Map<String, Object> learningProgress = chatBotService.getLearningProgress(loginUser.getId());
    return ResultUtils.success(learningProgress);
  }

  /**
   * 获取推荐题目
   *
   * @param request HTTP请求
   * @return 推荐题目列表
   */
  @GetMapping("/problems/recommended")
  public BaseResponse<List<Map<String, Object>>> getRecommendedProblems(
      HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    List<Map<String, Object>> recommendedProblems =
        chatBotService.getRecommendedProblems(loginUser.getId());
    return ResultUtils.success(recommendedProblems);
  }

  /**
   * 获取聊天历史
   *
   * @param questionId 问题ID（可选）
   * @param request HTTP请求
   * @return 聊天历史列表
   */
  @GetMapping("/chat/history")
  public BaseResponse<List<ChatMessage>> getChatHistory(
      @RequestParam(required = false) Long questionId, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    List<ChatMessage> chatHistory = chatBotService.getChatHistory(loginUser.getId(), questionId);
    return ResultUtils.success(chatHistory);
  }
}
