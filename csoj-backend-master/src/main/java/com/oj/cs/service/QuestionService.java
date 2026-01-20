package com.oj.cs.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.question.QuestionQueryRequest;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.vo.QuestionVO;

/**
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2025-08-07 20:58:00
 */
public interface QuestionService extends IService<Question> {

  /**
   * 校验
   *
   * @param question
   * @param add
   */
  void validQuestion(Question question, boolean add);

  /**
   * 获取查询条件
   *
   * @param questionQueryRequest
   * @return
   */
  QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

  /**
   * 获取题目封装
   *
   * @param question
   * @param request
   * @return
   */
  QuestionVO getQuestionVO(Question question, HttpServletRequest request);

  /**
   * 分页获取题目封装
   *
   * @param questionPage
   * @param request
   * @return
   */
  Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

  /**
   * 获取用户已完成的题目列表
   *
   * @param userId 用户ID
   * @return 已完成题目列表
   */
  List<Question> getCompletedQuestions(Long userId);

  /**
   * 获取推荐题目列表
   *
   * @param userId 用户ID
   * @param count 获取数量
   * @return 推荐题目列表
   */
  List<Question> getRecommendedQuestions(Long userId, int count);
}
