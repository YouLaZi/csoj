package com.oj.cs.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.enums.QuestionSubmitStatusEnum;
import com.oj.cs.service.QuestionSubmitService;

import lombok.extern.slf4j.Slf4j;

/** 异步判题任务 使用独立线程执行判题逻辑，避免阻塞主线程 */
@Slf4j
@Component
public class AsyncJudgeJob {

  @Autowired private com.oj.cs.judge.JudgeService judgeService;

  @Autowired private QuestionSubmitService questionSubmitService;

  /**
   * 异步执行判题
   *
   * @param questionSubmitId 提交记录ID
   * @param userId 用户ID（用于日志追踪）
   */
  @Async("judgeExecutor")
  public void asyncDoJudge(Long questionSubmitId, Long userId) {
    log.info("开始异步判题，提交ID: {}, 用户ID: {}", questionSubmitId, userId);

    // 更新状态为"判题中"
    QuestionSubmit updatingSubmit = new QuestionSubmit();
    updatingSubmit.setId(questionSubmitId);
    updatingSubmit.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
    boolean updateResult = questionSubmitService.updateById(updatingSubmit);
    if (!updateResult) {
      log.error("更新提交状态失败，提交ID: {}", questionSubmitId);
      return;
    }

    try {
      // 执行判题
      judgeService.doJudge(questionSubmitId);
      log.info("异步判题完成，提交ID: {}, 用户ID: {}", questionSubmitId, userId);
    } catch (Exception e) {
      log.error("异步判题失败，提交ID: {}, 用户ID: {}", questionSubmitId, userId, e);

      // 更新状态为"失败"
      QuestionSubmit failedSubmit = new QuestionSubmit();
      failedSubmit.setId(questionSubmitId);
      failedSubmit.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
      questionSubmitService.updateById(failedSubmit);
    }
  }
}
