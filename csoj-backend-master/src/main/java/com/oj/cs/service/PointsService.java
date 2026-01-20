package com.oj.cs.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.model.dto.points.PointsQueryRequest;
import com.oj.cs.model.entity.PointsRecord;
import com.oj.cs.model.entity.UserCheckin;
import com.oj.cs.model.vo.UserPointsRankVO;
import com.oj.cs.model.vo.UserPointsVO;

/** 积分系统服务 */
public interface PointsService {

  /**
   * 获取用户积分信息
   *
   * @param userId 用户ID
   * @return 用户积分信息
   */
  UserPointsVO getUserPoints(Long userId);

  /**
   * 获取用户积分记录
   *
   * @param userId 用户ID
   * @return 积分记录列表
   */
  List<PointsRecord> getUserPointsRecords(Long userId);

  /**
   * 分页获取用户积分记录
   *
   * @param pointsQueryRequest 查询条件
   * @return 分页结果
   */
  Page<PointsRecord> getPointsRecordsByPage(PointsQueryRequest pointsQueryRequest);

  /**
   * 获取积分排行榜
   *
   * @param timeRange 时间范围（day/week/month/all）
   * @param count 获取数量
   * @return 排行榜列表
   */
  List<UserPointsRankVO> getPointsRanking(String timeRange, int count);

  /**
   * 添加积分记录
   *
   * @param userId 用户ID
   * @param points 积分值
   * @param type 积分类型
   * @param description 描述
   * @param relatedId 关联ID
   * @return 积分记录ID
   */
  Long addPointsRecord(
      Long userId, Integer points, String type, String description, Long relatedId);

  /**
   * 用户签到
   *
   * @param userId 用户ID
   * @return 签到结果信息
   */
  Map<String, Object> userCheckin(Long userId);

  /**
   * 获取用户签到记录
   *
   * @param userId 用户ID
   * @param page 页码
   * @param pageSize 每页大小
   * @return 签到记录分页
   */
  Page<UserCheckin> getUserCheckinRecords(Long userId, long page, long pageSize);

  /**
   * 检查用户今日是否已签到
   *
   * @param userId 用户ID
   * @return 是否已签到
   */
  boolean hasCheckedInToday(Long userId);

  /**
   * 获取用户本月签到记录
   *
   * @param userId 用户ID
   * @return 签到日期列表
   */
  List<Date> getUserMonthlyCheckins(Long userId);
}
