package com.oj.cs.service.impl;

import java.util.*;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.MistakeNotebookMapper;
import com.oj.cs.model.dto.mistake.MistakeNotebookAddRequest;
import com.oj.cs.model.dto.mistake.MistakeNotebookQueryRequest;
import com.oj.cs.model.entity.MistakeNotebook;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.service.MistakeNotebookService;
import com.oj.cs.service.QuestionSubmitService;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/** 错题本服务实现 */
@Service
@Slf4j
public class MistakeNotebookServiceImpl extends ServiceImpl<MistakeNotebookMapper, MistakeNotebook>
    implements MistakeNotebookService {

  @Resource private QuestionSubmitService questionSubmitService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long addMistake(MistakeNotebookAddRequest request, Long userId) {
    if (request.getQuestionId() == null || request.getQuestionId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID不能为空");
    }

    // 检查是否已存在该题目的错题记录
    QueryWrapper<MistakeNotebook> wrapper = new QueryWrapper<>();
    wrapper.eq("userId", userId);
    wrapper.eq("questionId", request.getQuestionId());
    MistakeNotebook existing = this.getOne(wrapper);

    if (existing != null) {
      // 更新现有记录
      if (StrUtil.isNotBlank(request.getNotes())) {
        existing.setNotes(request.getNotes());
      }
      if (StrUtil.isNotBlank(request.getMistakeType())) {
        existing.setMistakeType(request.getMistakeType());
      }
      if (StrUtil.isNotBlank(request.getErrorMessage())) {
        existing.setErrorMessage(request.getErrorMessage());
      }
      if (StrUtil.isNotBlank(request.getCategory())) {
        existing.setCategory(request.getCategory());
      }
      if (request.getSubmitId() != null) {
        existing.setSubmitId(request.getSubmitId());
        // 获取提交代码
        QuestionSubmit submit = questionSubmitService.getById(request.getSubmitId());
        if (submit != null && StrUtil.isNotBlank(submit.getCode())) {
          existing.setUserCode(submit.getCode());
        }
      }
      // 重置复习状态
      existing.setIsReviewed(0);
      existing.setReviewCount(0);
      existing.setUpdateTime(new Date());
      this.updateById(existing);
      return existing.getId();
    }

    // 创建新的错题记录
    MistakeNotebook mistake = new MistakeNotebook();
    mistake.setUserId(userId);
    mistake.setQuestionId(request.getQuestionId());
    mistake.setSubmitId(request.getSubmitId());
    mistake.setMistakeType(request.getMistakeType());
    mistake.setErrorMessage(request.getErrorMessage());
    mistake.setNotes(request.getNotes());
    mistake.setCategory(request.getCategory());
    mistake.setIsReviewed(0);
    mistake.setReviewCount(0);
    mistake.setCreateTime(new Date());

    // 获取提交代码
    if (request.getSubmitId() != null) {
      QuestionSubmit submit = questionSubmitService.getById(request.getSubmitId());
      if (submit != null && StrUtil.isNotBlank(submit.getCode())) {
        mistake.setUserCode(submit.getCode());
      }
    }

    this.save(mistake);
    return mistake.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean updateNotes(Long id, String notes, Long userId) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    MistakeNotebook mistake = this.getById(id);
    if (mistake == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "错题记录不存在");
    }

    // 只能更新自己的错题
    if (!mistake.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改此记录");
    }

    mistake.setNotes(notes);
    mistake.setUpdateTime(new Date());
    return this.updateById(mistake);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean markAsReviewed(Long id, Long userId) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    MistakeNotebook mistake = this.getById(id);
    if (mistake == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "错题记录不存在");
    }

    // 只能更新自己的错题
    if (!mistake.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改此记录");
    }

    mistake.setIsReviewed(1);
    mistake.setReviewCount(mistake.getReviewCount() + 1);
    mistake.setLastReviewTime(new Date());
    mistake.setUpdateTime(new Date());
    return this.updateById(mistake);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean batchMarkAsReviewed(List<Long> ids, Long userId) {
    if (ids == null || ids.isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    for (Long id : ids) {
      try {
        markAsReviewed(id, userId);
      } catch (Exception e) {
        log.warn("标记错题 {} 为已复习失败: {}", id, e.getMessage());
      }
    }

    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean deleteMistake(Long id, Long userId) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    MistakeNotebook mistake = this.getById(id);
    if (mistake == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "错题记录不存在");
    }

    // 只能删除自己的错题
    if (!mistake.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除此记录");
    }

    return this.removeById(id);
  }

  @Override
  public IPage<MistakeNotebook> listByPage(MistakeNotebookQueryRequest request) {
    long current = request.getCurrent();
    long size = request.getPageSize();
    Page<MistakeNotebook> page = new Page<>(current, size);

    QueryWrapper<MistakeNotebook> queryWrapper = new QueryWrapper<>();

    if (request.getUserId() != null) {
      queryWrapper.eq("userId", request.getUserId());
    }
    if (request.getQuestionId() != null) {
      queryWrapper.eq("questionId", request.getQuestionId());
    }
    if (StrUtil.isNotBlank(request.getMistakeType())) {
      queryWrapper.eq("mistakeType", request.getMistakeType());
    }
    if (StrUtil.isNotBlank(request.getCategory())) {
      queryWrapper.eq("category", request.getCategory());
    }
    if (request.getIsReviewed() != null) {
      queryWrapper.eq("isReviewed", request.getIsReviewed());
    }

    // 排序
    String sortField =
        StrUtil.isNotBlank(request.getSortField()) ? request.getSortField() : "createTime";
    String sortOrder = StrUtil.isNotBlank(request.getSortOrder()) ? request.getSortOrder() : "desc";

    if ("asc".equalsIgnoreCase(sortOrder)) {
      queryWrapper.orderByAsc(sortField);
    } else {
      queryWrapper.orderByDesc(sortField);
    }

    return this.page(page, queryWrapper);
  }

  @Override
  public List<MistakeNotebook> getRemindList(Long userId) {
    if (userId == null || userId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    return this.baseMapper.getRemindList(userId, new Date());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean setRemindTime(Long id, Long remindTime, Long userId) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    MistakeNotebook mistake = this.getById(id);
    if (mistake == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "错题记录不存在");
    }

    // 只能设置自己的错题提醒
    if (!mistake.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改此记录");
    }

    Date remindDate = new Date(remindTime);
    mistake.setRemindTime(remindDate);
    mistake.setUpdateTime(new Date());
    return this.updateById(mistake);
  }

  @Override
  public MistakeStatistics getStatistics(Long userId) {
    if (userId == null || userId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    MistakeStatistics statistics = new MistakeStatistics();
    statistics.setUserId(userId);

    // 获取所有错题
    QueryWrapper<MistakeNotebook> wrapper = new QueryWrapper<>();
    wrapper.eq("userId", userId);
    List<MistakeNotebook> mistakes = this.list(wrapper);

    statistics.setTotalMistakes(mistakes.size());

    // 统计各类错误数量
    int notReviewedCount = 0;
    int reviewedCount = 0;
    int compileErrors = 0;
    int runtimeErrors = 0;
    int wrongAnswers = 0;
    int timeLimitExceeded = 0;

    for (MistakeNotebook mistake : mistakes) {
      if (mistake.getIsReviewed() != null && mistake.getIsReviewed() == 1) {
        reviewedCount++;
      } else {
        notReviewedCount++;
      }

      String mistakeType = mistake.getMistakeType();
      if ("COMPILE_ERROR".equals(mistakeType)) {
        compileErrors++;
      } else if ("RUNTIME_ERROR".equals(mistakeType)) {
        runtimeErrors++;
      } else if ("WRONG_ANSWER".equals(mistakeType)) {
        wrongAnswers++;
      } else if ("TIME_LIMIT_EXCEEDED".equals(mistakeType)) {
        timeLimitExceeded++;
      }
    }

    statistics.setNotReviewedCount(notReviewedCount);
    statistics.setReviewedCount(reviewedCount);
    statistics.setCompileErrors(compileErrors);
    statistics.setRuntimeErrors(runtimeErrors);
    statistics.setWrongAnswers(wrongAnswers);
    statistics.setTimeLimitExceeded(timeLimitExceeded);

    return statistics;
  }
}
