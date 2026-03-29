package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.entity.ChallengeStreak;
import com.oj.cs.model.entity.DailyChallenge;
import com.oj.cs.model.entity.UserChallenge;
import com.oj.cs.service.DailyChallengeService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 每日挑战接口 */
@RestController
@RequestMapping("/challenge")
@Slf4j
public class DailyChallengeController {

  @Resource private DailyChallengeService dailyChallengeService;

  @Resource private UserService userService;

  /** 获取今日挑战 */
  @GetMapping("/today")
  public BaseResponse<DailyChallenge> getTodayChallenge() {
    DailyChallenge challenge = dailyChallengeService.getTodayChallenge();
    return ResultUtils.success(challenge);
  }

  /** 完成挑战 */
  @PostMapping("/complete")
  public BaseResponse<Integer> completeChallenge(
      @RequestParam Long challengeId,
      @RequestParam(defaultValue = "false") boolean isPerfect,
      HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    int pointsEarned = dailyChallengeService.completeChallenge(userId, challengeId, isPerfect);
    return ResultUtils.success(pointsEarned);
  }

  /** 获取用户挑战状态 */
  @GetMapping("/status/{challengeId}")
  public BaseResponse<UserChallenge> getUserChallengeStatus(
      @PathVariable Long challengeId, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserChallenge status = dailyChallengeService.getUserChallengeStatus(userId, challengeId);
    return ResultUtils.success(status);
  }

  /** 获取用户连胜信息 */
  @GetMapping("/streak")
  public BaseResponse<ChallengeStreak> getUserStreak(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    ChallengeStreak streak = dailyChallengeService.getUserStreak(userId);
    return ResultUtils.success(streak);
  }

  /** 获取挑战历史 */
  @GetMapping("/history")
  public BaseResponse<List<UserChallenge>> getChallengeHistory(
      @RequestParam(defaultValue = "30") int days, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    List<UserChallenge> history = dailyChallengeService.getChallengeHistory(userId, days);
    return ResultUtils.success(history);
  }
}
