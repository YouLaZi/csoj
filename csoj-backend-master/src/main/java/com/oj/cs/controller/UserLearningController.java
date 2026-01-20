package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.QuestionService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 用户学习相关接口 注意：用户学习进度和推荐题目接口已移至ChatBotController */
@RestController
@RequestMapping("/learning")
@Slf4j
public class UserLearningController {

  @Resource private QuestionService questionService;

  @Resource private UserService userService;

  /**
   * 获取用户已完成的题目列表
   *
   * @param request HTTP请求
   * @return 已完成题目列表
   */
  @GetMapping("/completed-problems")
  public BaseResponse<List<Question>> getCompletedProblems(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    List<Question> completedProblems = questionService.getCompletedQuestions(loginUser.getId());
    return ResultUtils.success(completedProblems);
  }
}
