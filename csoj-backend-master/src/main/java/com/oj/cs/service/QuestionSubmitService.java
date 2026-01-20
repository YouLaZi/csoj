package com.oj.cs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.oj.cs.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.QuestionSubmitVO;

/**
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2025-08-07 20:58:53
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

  /**
   * 题目提交
   *
   * @param questionSubmitAddRequest 题目提交信息
   * @param loginUser
   * @return
   */
  long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

  /**
   * 获取查询条件
   *
   * @param questionSubmitQueryRequest
   * @return
   */
  QueryWrapper<QuestionSubmit> getQueryWrapper(
      QuestionSubmitQueryRequest questionSubmitQueryRequest);

  /**
   * 获取题目封装
   *
   * @param questionSubmit
   * @param loginUser
   * @return
   */
  QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

  /**
   * 分页获取题目封装
   *
   * @param questionSubmitPage
   * @param loginUser
   * @return
   */
  Page<QuestionSubmitVO> getQuestionSubmitVOPage(
      Page<QuestionSubmit> questionSubmitPage, User loginUser);

  /**
   * 检查用户是否已通过某题目
   *
   * @param userId 用户ID
   * @param questionId 题目ID
   * @return 是否已通过
   */
  boolean hasUserAcceptedQuestion(Long userId, Long questionId);
}
