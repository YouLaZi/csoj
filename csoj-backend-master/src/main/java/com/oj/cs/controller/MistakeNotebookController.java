package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.dto.mistake.MistakeNotebookAddRequest;
import com.oj.cs.model.dto.mistake.MistakeNotebookQueryRequest;
import com.oj.cs.model.entity.MistakeNotebook;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.MistakeNotebookService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 错题本接口 */
@RestController
@RequestMapping("/mistake")
@Slf4j
public class MistakeNotebookController {

  @Resource private MistakeNotebookService mistakeNotebookService;

  @Resource private UserService userService;

  /** 添加错题记录 */
  @PostMapping("/add")
  public BaseResponse<Long> addMistake(
      @RequestBody MistakeNotebookAddRequest request, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    Long id = mistakeNotebookService.addMistake(request, loginUser.getId());
    return ResultUtils.success(id);
  }

  /** 更新错题笔记 */
  @PostMapping("/update/notes")
  public BaseResponse<Boolean> updateNotes(
      @RequestParam Long id, @RequestParam String notes, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Boolean result = mistakeNotebookService.updateNotes(id, notes, loginUser.getId());
    return ResultUtils.success(result);
  }

  /** 标记错题为已复习 */
  @PostMapping("/review")
  public BaseResponse<Boolean> markAsReviewed(@RequestParam Long id, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Boolean result = mistakeNotebookService.markAsReviewed(id, loginUser.getId());
    return ResultUtils.success(result);
  }

  /** 批量标记错题为已复习 */
  @PostMapping("/review/batch")
  public BaseResponse<Boolean> batchMarkAsReviewed(
      @RequestBody List<Long> ids, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Boolean result = mistakeNotebookService.batchMarkAsReviewed(ids, loginUser.getId());
    return ResultUtils.success(result);
  }

  /** 删除错题记录 */
  @PostMapping("/delete")
  public BaseResponse<Boolean> deleteMistake(@RequestParam Long id, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Boolean result = mistakeNotebookService.deleteMistake(id, loginUser.getId());
    return ResultUtils.success(result);
  }

  /** 分页查询错题记录 */
  @PostMapping("/list/page")
  public BaseResponse<IPage<MistakeNotebook>> listByPage(
      @RequestBody MistakeNotebookQueryRequest request, HttpServletRequest httpRequest) {
    // 如果没有指定用户ID，查询当前用户的错题
    if (request.getUserId() == null) {
      User loginUser = userService.getLoginUser(httpRequest);
      request.setUserId(loginUser.getId());
    }
    IPage<MistakeNotebook> page = mistakeNotebookService.listByPage(request);
    return ResultUtils.success(page);
  }

  /** 获取需要复习提醒的错题列表 */
  @GetMapping("/remind")
  public BaseResponse<List<MistakeNotebook>> getRemindList(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    List<MistakeNotebook> list = mistakeNotebookService.getRemindList(loginUser.getId());
    return ResultUtils.success(list);
  }

  /** 设置复习提醒时间 */
  @PostMapping("/remind/set")
  public BaseResponse<Boolean> setRemindTime(
      @RequestParam Long id, @RequestParam Long remindTime, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Boolean result = mistakeNotebookService.setRemindTime(id, remindTime, loginUser.getId());
    return ResultUtils.success(result);
  }

  /** 获取错题统计 */
  @GetMapping("/statistics")
  public BaseResponse<MistakeNotebookService.MistakeStatistics> getStatistics(
      HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    MistakeNotebookService.MistakeStatistics statistics =
        mistakeNotebookService.getStatistics(loginUser.getId());
    return ResultUtils.success(statistics);
  }

  /** 获取错题详情 */
  @GetMapping("/get")
  public BaseResponse<MistakeNotebook> getMistake(@RequestParam Long id) {
    MistakeNotebook mistake = mistakeNotebookService.getById(id);
    return ResultUtils.success(mistake);
  }
}
