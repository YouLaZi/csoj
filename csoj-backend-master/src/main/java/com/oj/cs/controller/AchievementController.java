package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.entity.Achievement;
import com.oj.cs.model.entity.UserAchievement;
import com.oj.cs.service.AchievementService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 成就系统接口 */
@RestController
@RequestMapping("/achievement")
@Slf4j
public class AchievementController {

  @Resource private AchievementService achievementService;

  @Resource private UserService userService;

  /** 获取用户所有成就 */
  @GetMapping
  public BaseResponse<List<UserAchievement>> getUserAchievements(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    List<UserAchievement> achievements = achievementService.getUserAchievements(userId);
    return ResultUtils.success(achievements);
  }

  /** 获取指定分类的成就 */
  @GetMapping("/category/{category}")
  public BaseResponse<List<UserAchievement>> getAchievementsByCategory(
      @PathVariable String category, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    List<UserAchievement> achievements =
        achievementService.getUserAchievementsByCategory(userId, category);
    return ResultUtils.success(achievements);
  }

  /** 获取所有成就定义 */
  @GetMapping("/definitions")
  public BaseResponse<List<Achievement>> getAllAchievements() {
    List<Achievement> achievements = achievementService.getAllAchievements();
    return ResultUtils.success(achievements);
  }

  /** 获取未读成就数量 */
  @GetMapping("/unread/count")
  public BaseResponse<Integer> getUnreadCount(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    int count = achievementService.getUnreadCount(userId);
    return ResultUtils.success(count);
  }

  /** 标记成就为已读 */
  @PostMapping("/read/{achievementCode}")
  public BaseResponse<Boolean> markAsRead(
      @PathVariable String achievementCode, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    achievementService.markAsRead(userId, achievementCode);
    return ResultUtils.success(true);
  }
}
