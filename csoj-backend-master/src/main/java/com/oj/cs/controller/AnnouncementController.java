package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.entity.Announcement;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.AnnouncementService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 系统公告接口 */
@RestController
@RequestMapping("/announcement")
@Slf4j
public class AnnouncementController {

  @Resource private AnnouncementService announcementService;

  @Resource private UserService userService;

  /**
   * 获取系统公告列表
   *
   * @param page 页码
   * @param pageSize 每页大小
   * @return 公告列表
   */
  @GetMapping("/list")
  public BaseResponse<Page<Announcement>> getAnnouncementList(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long pageSize) {
    if (page <= 0 || pageSize <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Page<Announcement> announcementPage = announcementService.getAnnouncementList(page, pageSize);
    return ResultUtils.success(announcementPage);
  }

  /**
   * 获取最新公告
   *
   * @param count 获取数量
   * @return 公告列表
   */
  @GetMapping("/latest")
  public BaseResponse<List<Announcement>> getLatestAnnouncements(
      @RequestParam(defaultValue = "5") int count) {
    if (count <= 0 || count > 20) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取数量应在1-20之间");
    }
    List<Announcement> announcements = announcementService.getLatestAnnouncements(count);
    return ResultUtils.success(announcements);
  }

  /**
   * 创建公告
   *
   * @param announcement 公告信息
   * @param request HTTP请求
   * @return 创建的公告ID
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = "admin")
  public BaseResponse<Long> addAnnouncement(
      @RequestBody Announcement announcement, HttpServletRequest request) {
    if (announcement == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    long announcementId = announcementService.createAnnouncement(announcement, loginUser.getId());
    return ResultUtils.success(announcementId);
  }

  /**
   * 更新公告
   *
   * @param announcement 公告信息
   * @param request HTTP请求
   * @return 是否成功
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = "admin")
  public BaseResponse<Boolean> updateAnnouncement(
      @RequestBody Announcement announcement, HttpServletRequest request) {
    if (announcement == null || announcement.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    boolean result = announcementService.updateAnnouncement(announcement, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 删除公告
   *
   * @param id 公告ID
   * @param request HTTP请求
   * @return 是否成功
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = "admin")
  public BaseResponse<Boolean> deleteAnnouncement(
      @RequestParam long id, HttpServletRequest request) {
    if (id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    boolean result = announcementService.deleteAnnouncement(id, loginUser.getId());
    return ResultUtils.success(result);
  }
}
