package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.PointsConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.vo.UserPointsRankVO;
import com.oj.cs.service.PointsService;

import lombok.extern.slf4j.Slf4j;

/** 积分排行榜接口 */
@RestController
@RequestMapping("/leaderboard")
@Slf4j
public class LeaderboardController {

  @Resource private PointsService pointsService;

  /**
   * 获取积分排行榜
   *
   * @param timeRange 时间范围（day/week/month/all）
   * @param count 获取数量，默认为10
   * @return 排行榜列表
   */
  @GetMapping("/points")
  public BaseResponse<List<UserPointsRankVO>> getPointsRanking(
      @RequestParam(value = "timeRange", defaultValue = "all") String timeRange,
      @RequestParam(value = "count", defaultValue = "10") Integer count) {
    // 校验参数
    if (count <= 0 || count > 100) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "获取数量应在1-100之间");
    }

    // 校验时间范围参数
    if (!StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_DAY)
        && !StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_WEEK)
        && !StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_MONTH)
        && !StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_ALL)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "时间范围参数错误");
    }

    List<UserPointsRankVO> rankList = pointsService.getPointsRanking(timeRange, count);
    return ResultUtils.success(rankList);
  }
}
