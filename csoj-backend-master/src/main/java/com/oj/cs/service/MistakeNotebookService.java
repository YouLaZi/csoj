package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.mistake.MistakeNotebookAddRequest;
import com.oj.cs.model.dto.mistake.MistakeNotebookQueryRequest;
import com.oj.cs.model.entity.MistakeNotebook;

/** 错题本服务接口 */
public interface MistakeNotebookService extends IService<MistakeNotebook> {

  /** 添加错题记录 如果该题已存在错题记录，则更新 */
  Long addMistake(MistakeNotebookAddRequest request, Long userId);

  /** 更新错题笔记 */
  Boolean updateNotes(Long id, String notes, Long userId);

  /** 标记错题为已复习 */
  Boolean markAsReviewed(Long id, Long userId);

  /** 批量标记错题为已复习 */
  Boolean batchMarkAsReviewed(List<Long> ids, Long userId);

  /** 删除错题记录 */
  Boolean deleteMistake(Long id, Long userId);

  /** 分页查询错题记录 */
  IPage<MistakeNotebook> listByPage(MistakeNotebookQueryRequest request);

  /** 获取需要复习提醒的错题列表 */
  List<MistakeNotebook> getRemindList(Long userId);

  /** 设置复习提醒时间 */
  Boolean setRemindTime(Long id, Long remindTime, Long userId);

  /** 获取用户错题统计 */
  MistakeStatistics getStatistics(Long userId);

  /** 错题统计信息 */
  class MistakeStatistics {
    private Long userId;
    private Integer totalMistakes; // 总错题数
    private Integer notReviewedCount; // 未复习数量
    private Integer reviewedCount; // 已复习数量
    private Integer compileErrors; // 编译错误数
    private Integer runtimeErrors; // 运行时错误数
    private Integer wrongAnswers; // 答案错误数
    private Integer timeLimitExceeded; // 超时数

    // Getters and Setters
    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public Integer getTotalMistakes() {
      return totalMistakes;
    }

    public void setTotalMistakes(Integer totalMistakes) {
      this.totalMistakes = totalMistakes;
    }

    public Integer getNotReviewedCount() {
      return notReviewedCount;
    }

    public void setNotReviewedCount(Integer notReviewedCount) {
      this.notReviewedCount = notReviewedCount;
    }

    public Integer getReviewedCount() {
      return reviewedCount;
    }

    public void setReviewedCount(Integer reviewedCount) {
      this.reviewedCount = reviewedCount;
    }

    public Integer getCompileErrors() {
      return compileErrors;
    }

    public void setCompileErrors(Integer compileErrors) {
      this.compileErrors = compileErrors;
    }

    public Integer getRuntimeErrors() {
      return runtimeErrors;
    }

    public void setRuntimeErrors(Integer runtimeErrors) {
      this.runtimeErrors = runtimeErrors;
    }

    public Integer getWrongAnswers() {
      return wrongAnswers;
    }

    public void setWrongAnswers(Integer wrongAnswers) {
      this.wrongAnswers = wrongAnswers;
    }

    public Integer getTimeLimitExceeded() {
      return timeLimitExceeded;
    }

    public void setTimeLimitExceeded(Integer timeLimitExceeded) {
      this.timeLimitExceeded = timeLimitExceeded;
    }
  }
}
