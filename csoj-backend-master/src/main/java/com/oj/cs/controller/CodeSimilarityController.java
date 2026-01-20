package com.oj.cs.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.model.dto.similarity.CodeSimilarityQueryRequest;
import com.oj.cs.model.entity.CodeSimilarity;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.CodeSimilarityService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 代码查重接口 */
@RestController
@RequestMapping("/similarity")
@Slf4j
public class CodeSimilarityController {

  @Resource private CodeSimilarityService codeSimilarityService;

  @Resource private UserService userService;

  /** 检测题目代码相似度（教师/管理员） */
  @PostMapping("/detect")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Integer> detectSimilarity(
      @RequestParam Long questionId,
      @RequestParam(defaultValue = "50") Integer threshold,
      HttpServletRequest request) {
    // 检查是否是教师或管理员
    User loginUser = userService.getLoginUser(request);
    if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())
        && !UserConstant.TEACHER_ROLE.equals(loginUser.getUserRole())) {
      return ResultUtils.error(403, "无权限");
    }
    int count = codeSimilarityService.detectSimilarityForQuestion(questionId, threshold);
    return ResultUtils.success(count);
  }

  /** 比较两次提交的相似度 */
  @GetMapping("/compare")
  public BaseResponse<CodeSimilarity> compareSubmits(
      @RequestParam Long submitId1, @RequestParam Long submitId2) {
    CodeSimilarity similarity = codeSimilarityService.compareSubmits(submitId1, submitId2);
    return ResultUtils.success(similarity);
  }

  /** 分页查询相似记录 */
  @PostMapping("/list/page")
  public BaseResponse<IPage<CodeSimilarity>> listSimilarityByPage(
      @RequestBody CodeSimilarityQueryRequest queryRequest, HttpServletRequest request) {
    // 如果不是管理员，只能查看自己相关的相似记录
    User loginUser = userService.getLoginUser(request);
    if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())
        && !UserConstant.TEACHER_ROLE.equals(loginUser.getUserRole())) {
      queryRequest.setUserId(loginUser.getId());
    }

    IPage<CodeSimilarity> page = codeSimilarityService.listSimilarityByPage(queryRequest);
    return ResultUtils.success(page);
  }

  /** 获取相似记录详情 */
  @GetMapping("/get")
  public BaseResponse<CodeSimilarity> getSimilarity(@RequestParam Long id) {
    CodeSimilarity similarity = codeSimilarityService.getById(id);
    return ResultUtils.success(similarity);
  }

  /** 标记相似记录为已处理（教师/管理员） */
  @PostMapping("/process")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> markAsProcessed(
      @RequestParam Long id, @RequestParam String processResult, HttpServletRequest request) {
    // 检查是否是教师或管理员
    User loginUser = userService.getLoginUser(request);
    if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())
        && !UserConstant.TEACHER_ROLE.equals(loginUser.getUserRole())) {
      return ResultUtils.error(403, "无权限");
    }
    Boolean result = codeSimilarityService.markAsProcessed(id, processResult);
    return ResultUtils.success(result);
  }

  /** 获取题目相似度统计（教师/管理员） */
  @GetMapping("/statistics")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<CodeSimilarityService.SimilarityStatistics> getStatistics(
      @RequestParam Long questionId, HttpServletRequest request) {
    // 检查是否是教师或管理员
    User loginUser = userService.getLoginUser(request);
    if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())
        && !UserConstant.TEACHER_ROLE.equals(loginUser.getUserRole())) {
      return ResultUtils.error(403, "无权限");
    }
    CodeSimilarityService.SimilarityStatistics statistics =
        codeSimilarityService.getSimilarityStatistics(questionId);
    return ResultUtils.success(statistics);
  }
}
