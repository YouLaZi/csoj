package com.oj.cs.controller;

import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.entity.UserCheckin;
import com.oj.cs.service.PointsService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 用户签到接口 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserCheckinController {

  @Resource private PointsService pointsService;

  @Resource private UserService userService;

  /**
   * 用户签到
   *
   * @param request HTTP请求
   * @return 签到结果
   */
  @PostMapping("/checkin")
  public BaseResponse<Map<String, Object>> userCheckin(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Map<String, Object> result = pointsService.userCheckin(loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 获取用户签到记录
   *
   * @param page 页码
   * @param pageSize 每页大小
   * @param request HTTP请求
   * @return 签到记录列表
   */
  @GetMapping("/checkin/records")
  public BaseResponse<Page<UserCheckin>> getUserCheckinRecords(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "10") long pageSize,
      HttpServletRequest request) {
    if (page <= 0 || pageSize <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Page<UserCheckin> checkinRecords =
        pointsService.getUserCheckinRecords(loginUser.getId(), page, pageSize);
    return ResultUtils.success(checkinRecords);
  }
}
