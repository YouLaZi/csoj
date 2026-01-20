package com.oj.cs.controller;

import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.dto.points.PointsQueryRequest;
import com.oj.cs.model.entity.PointsRecord;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.UserPointsVO;
import com.oj.cs.service.PointsService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 积分接口 */
@RestController
@RequestMapping("/points")
@Slf4j
public class PointsController {

  @Resource private PointsService pointsService;

  @Resource private UserService userService;

  /**
   * 获取用户积分信息
   *
   * @param request 请求
   * @return 用户积分信息
   */
  @GetMapping("/info")
  public BaseResponse<UserPointsVO> getUserPointsInfo(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    UserPointsVO userPointsVO = pointsService.getUserPoints(loginUser.getId());
    return ResultUtils.success(userPointsVO);
  }

  /**
   * 分页获取用户积分记录
   *
   * @param pointsQueryRequest 查询条件
   * @param request 请求
   * @return 分页结果
   */
  @GetMapping("/records/page")
  public BaseResponse<Page<PointsRecord>> getPointsRecordsByPage(
      PointsQueryRequest pointsQueryRequest, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    pointsQueryRequest.setUserId(loginUser.getId());
    Page<PointsRecord> page = pointsService.getPointsRecordsByPage(pointsQueryRequest);
    return ResultUtils.success(page);
  }

  /**
   * 获取用户积分记录
   *
   * @param request 请求
   * @return 用户积分记录列表
   */
  @GetMapping("/records")
  public BaseResponse<List<PointsRecord>> getUserPointsRecords(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    List<PointsRecord> records = pointsService.getUserPointsRecords(loginUser.getId());
    return ResultUtils.success(records);
  }

  /**
   * 用户签到
   *
   * @param request 请求
   * @return 签到结果
   */
  @PostMapping("/checkin")
  public BaseResponse<Boolean> userCheckin(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    // Assuming pointsService.userCheckin returns a Map e.g. Map<String, Object>
    // And the actual boolean status is within this map, under a key like "success".
    // This change handles that assumption.
    Object serviceResult = pointsService.userCheckin(loginUser.getId());
    boolean actualResult;
    if (serviceResult instanceof Boolean) {
      actualResult = (Boolean) serviceResult;
    } else if (serviceResult instanceof java.util.Map) {
      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> resultMap = (java.util.Map<String, Object>) serviceResult;
      actualResult = (Boolean) resultMap.getOrDefault("success", false);
    } else {
      // Handle unexpected type, perhaps log an error and default
      // For now, defaulting to false and logging to console (in a real app, use a logger)
      System.err.println(
          "Unexpected return type from pointsService.userCheckin: "
              + (serviceResult != null ? serviceResult.getClass().getName() : "null"));
      actualResult = false;
    }
    return ResultUtils.success(actualResult);
  }

  /**
   * 检查用户今日是否已签到
   *
   * @param request 请求
   * @return 是否已签到
   */
  @GetMapping("/checkin/status")
  public BaseResponse<Boolean> checkUserCheckinStatus(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    boolean hasCheckedIn = pointsService.hasCheckedInToday(loginUser.getId());
    return ResultUtils.success(hasCheckedIn);
  }

  /**
   * 获取用户本月签到记录
   *
   * @param request 请求
   * @return 签到日期列表
   */
  @GetMapping("/checkin/monthly")
  public BaseResponse<List<Date>> getUserMonthlyCheckins(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    List<Date> checkinDates = pointsService.getUserMonthlyCheckins(loginUser.getId());
    return ResultUtils.success(checkinDates);
  }
}
