package com.oj.cs.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.constant.CommonConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.judge.JudgeService;
import com.oj.cs.judge.codesandbox.model.JudgeInfo;
import com.oj.cs.mapper.QuestionSubmitMapper;
import com.oj.cs.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.oj.cs.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.enums.JudgeInfoMessageEnum;
import com.oj.cs.model.enums.QuestionSubmitLanguageEnum;
import com.oj.cs.model.enums.QuestionSubmitStatusEnum;
import com.oj.cs.model.vo.QuestionSubmitVO;
import com.oj.cs.service.QuestionService;
import com.oj.cs.service.QuestionSubmitService;
import com.oj.cs.service.UserService;
import com.oj.cs.utils.CodeSecurityUtils;
import com.oj.cs.utils.SqlUtils;

import cn.hutool.json.JSONUtil;

/**
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2025-08-07 20:58:53
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService {

  @Resource private QuestionService questionService;

  @Resource private UserService userService;

  @Resource @Lazy private JudgeService judgeService;

  @Resource private QuestionSubmitMapper questionSubmitMapper;

  /**
   * 提交题目
   *
   * @param questionSubmitAddRequest
   * @param loginUser
   * @return
   */
  @Override
  public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
    // 校验编程语言是否合法
    String language = questionSubmitAddRequest.getLanguage();
    QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
    if (languageEnum == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
    }

    // 代码安全检查
    String code = questionSubmitAddRequest.getCode();
    CodeSecurityUtils.checkCodeSecurity(code);
    CodeSecurityUtils.validateCodeLength(code, 65535);

    // 验证编程语言支持
    if (!CodeSecurityUtils.isSupportedLanguage(language)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的编程语言: " + language);
    }
    long questionId = questionSubmitAddRequest.getQuestionId();
    // 判断实体是否存在，根据类别获取实体
    Question question = questionService.getById(questionId);
    if (question == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }
    // 是否已提交题目
    long userId = loginUser.getId();
    // 每个用户串行提交题目
    QuestionSubmit questionSubmit = new QuestionSubmit();
    questionSubmit.setUserId(userId);
    questionSubmit.setQuestionId(questionId);
    questionSubmit.setCode(questionSubmitAddRequest.getCode());
    questionSubmit.setLanguage(language);
    // 设置初始状态
    questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
    questionSubmit.setJudgeInfo("{}");
    boolean save = this.save(questionSubmit);
    if (!save) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
    }
    Long questionSubmitId = questionSubmit.getId();
    // 执行判题服务
    CompletableFuture.runAsync(
        () -> {
          try {
            judgeService.doJudge(questionSubmitId);
          } catch (Exception e) {
            // 记录异常日志，防止异步任务中的异常被吞掉
            log.error("异步判题任务执行异常，题目提交ID: " + questionSubmitId, e);
            // 尝试更新题目提交状态为失败
            try {
              QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
              questionSubmitUpdate.setId(questionSubmitId);
              questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
              JudgeInfo judgeInfo = new JudgeInfo();
              judgeInfo.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getText());
              questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
              boolean updateResult = questionSubmitMapper.updateById(questionSubmitUpdate) > 0;
              if (!updateResult) {
                log.error("更新题目提交状态失败，题目提交ID: " + questionSubmitId);
              }
            } catch (Exception updateException) {
              log.error("更新题目提交状态异常，题目提交ID: " + questionSubmitId, updateException);
            }
          }
        });
    return questionSubmitId;
  }

  /**
   * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
   *
   * @param questionSubmitQueryRequest
   * @return
   */
  @Override
  public QueryWrapper<QuestionSubmit> getQueryWrapper(
      QuestionSubmitQueryRequest questionSubmitQueryRequest) {
    QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
    if (questionSubmitQueryRequest == null) {
      return queryWrapper;
    }
    String language = questionSubmitQueryRequest.getLanguage();
    Integer status = questionSubmitQueryRequest.getStatus();
    Long questionId = questionSubmitQueryRequest.getQuestionId();
    Long userId = questionSubmitQueryRequest.getUserId();
    String sortField = questionSubmitQueryRequest.getSortField();
    String sortOrder = questionSubmitQueryRequest.getSortOrder();

    // 拼接查询条件
    queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
    queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
    queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
    queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
    queryWrapper.eq("isDelete", false);
    queryWrapper.orderBy(
        SqlUtils.validSortField(sortField),
        sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
        sortField);
    return queryWrapper;
  }

  @Override
  public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
    QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
    // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
    long userId = loginUser.getId();
    // 处理脱敏
    if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
      questionSubmitVO.setCode(null);
    }
    return questionSubmitVO;
  }

  @Override
  public Page<QuestionSubmitVO> getQuestionSubmitVOPage(
      Page<QuestionSubmit> questionSubmitPage, User loginUser) {
    List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
    Page<QuestionSubmitVO> questionSubmitVOPage =
        new Page<>(
            questionSubmitPage.getCurrent(),
            questionSubmitPage.getSize(),
            questionSubmitPage.getTotal());
    if (CollectionUtils.isEmpty(questionSubmitList)) {
      return questionSubmitVOPage;
    }
    List<QuestionSubmitVO> questionSubmitVOList =
        questionSubmitList.stream()
            .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
            .collect(Collectors.toList());
    questionSubmitVOPage.setRecords(questionSubmitVOList);
    return questionSubmitVOPage;
  }

  /**
   * 检查用户是否已通过某题目
   *
   * @param userId 用户ID
   * @param questionId 题目ID
   * @return 是否已通过
   */
  @Override
  public boolean hasUserAcceptedQuestion(Long userId, Long questionId) {
    if (userId == null || questionId == null) {
      return false;
    }

    // 查询用户是否有通过的提交记录
    QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("user_id", userId);
    queryWrapper.eq("question_id", questionId);
    queryWrapper.eq("status", QuestionSubmitStatusEnum.SUCCEED.getValue());
    queryWrapper.eq("is_delete", 0);
    queryWrapper.last("limit 1");

    return this.count(queryWrapper) > 0;
  }
}
