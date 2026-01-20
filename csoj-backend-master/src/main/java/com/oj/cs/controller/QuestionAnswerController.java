package com.oj.cs.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.QuestionService;
import com.oj.cs.service.QuestionSubmitService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 题目答案接口 */
@RestController
@RequestMapping("/question/answer")
@Slf4j
public class QuestionAnswerController {

  @Resource private QuestionService questionService;

  @Resource private UserService userService;

  @Resource private QuestionSubmitService questionSubmitService;

  /**
   * 检查用户是否有权限查看答案
   *
   * @param questionId 题目ID
   * @param request HTTP请求
   * @return 是否有权限
   */
  @GetMapping("/check-permission")
  public BaseResponse<Boolean> checkAnswerPermission(
      @RequestParam Long questionId, HttpServletRequest request) {
    if (questionId == null || questionId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);

    // 1. 管理员有权限查看所有答案
    if (userService.isAdmin(loginUser)) {
      return ResultUtils.success(true);
    }

    // 2. 题目创建者有权限查看答案
    Question question = questionService.getById(questionId);
    ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
    if (question.getUserId().equals(loginUser.getId())) {
      return ResultUtils.success(true);
    }

    // 3. 已通过题目的用户有权限查看答案
    boolean hasAccepted =
        questionSubmitService.hasUserAcceptedQuestion(loginUser.getId(), questionId);
    if (hasAccepted) {
      return ResultUtils.success(true);
    }

    // 默认无权限
    return ResultUtils.success(false);
  }

  /**
   * 获取题目答案
   *
   * @param questionId 题目ID
   * @param request HTTP请求
   * @return 题目答案
   */
  @GetMapping
  public BaseResponse<String> getQuestionAnswer(
      @RequestParam Long questionId, HttpServletRequest request) {
    if (questionId == null || questionId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 获取当前登录用户
    User loginUser = userService.getLoginUser(request);

    // 检查权限
    boolean hasPermission = false;

    // 1. 管理员有权限查看所有答案
    if (userService.isAdmin(loginUser)) {
      hasPermission = true;
    } else {
      // 2. 题目创建者有权限查看答案
      Question question = questionService.getById(questionId);
      ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
      if (question.getUserId().equals(loginUser.getId())) {
        hasPermission = true;
      } else {
        // 3. 已通过题目的用户有权限查看答案
        boolean hasAccepted =
            questionSubmitService.hasUserAcceptedQuestion(loginUser.getId(), questionId);
        if (hasAccepted) {
          hasPermission = true;
        }
      }
    }

    // 如果没有权限，抛出异常
    ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR, "您没有权限查看此题答案");

    // 获取题目答案
    Question question = questionService.getById(questionId);
    ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);

    String answer = question.getAnswer();
    return ResultUtils.success(answer);
  }
}
