package com.oj.cs.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.constant.PointsConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.mapper.PointsRecordMapper;
import com.oj.cs.mapper.UserCheckinMapper;
import com.oj.cs.mapper.UserPointsMapper;
import com.oj.cs.model.dto.points.PointsQueryRequest;
import com.oj.cs.model.entity.PointsRecord;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.entity.UserCheckin;
import com.oj.cs.model.entity.UserPoints;
import com.oj.cs.model.vo.UserPointsRankVO;
import com.oj.cs.model.vo.UserPointsVO;
import com.oj.cs.service.PointsService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 积分系统服务实现 */
@Service
@Slf4j
public class PointsServiceImpl implements PointsService {

  @Resource private UserPointsMapper userPointsMapper;

  @Resource private PointsRecordMapper pointsRecordMapper;

  @Resource private UserCheckinMapper userCheckinMapper;

  @Resource private UserService userService;

  @Override
  public UserPointsVO getUserPoints(Long userId) {
    // 校验用户是否存在
    userService.getById(userId);

    // 查询用户积分
    UserPoints userPoints = getUserPointsByUserId(userId);

    // 查询用户积分记录
    List<PointsRecord> pointsRecords = getUserPointsRecords(userId);

    // 组装VO
    UserPointsVO userPointsVO = new UserPointsVO();
    userPointsVO.setTotalPoints(userPoints.getTotalPoints());
    userPointsVO.setRecords(pointsRecords);

    return userPointsVO;
  }

  @Override
  public List<PointsRecord> getUserPointsRecords(Long userId) {
    // 查询用户积分记录
    LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(PointsRecord::getUserId, userId).orderByDesc(PointsRecord::getCreateTime);
    return pointsRecordMapper.selectList(queryWrapper);
  }

  @Override
  public Page<PointsRecord> getPointsRecordsByPage(PointsQueryRequest pointsQueryRequest) {
    // 参数校验
    ThrowUtils.throwIf(
        pointsQueryRequest == null, new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空"));

    // 获取分页参数
    long current = pointsQueryRequest.getCurrent();
    long pageSize = pointsQueryRequest.getPageSize();

    // 构建查询条件
    LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();

    // 用户ID条件
    Long userId = pointsQueryRequest.getUserId();
    if (userId != null && userId > 0) {
      queryWrapper.eq(PointsRecord::getUserId, userId);
    }

    // 积分类型条件
    String type = pointsQueryRequest.getType();
    if (StringUtils.isNotBlank(type)) {
      queryWrapper.eq(PointsRecord::getType, type);
    }

    // 处理时间范围
    String timeRange = pointsQueryRequest.getTimeRange();
    Date startTime = pointsQueryRequest.getStartTime();
    Date endTime = pointsQueryRequest.getEndTime();

    // 如果指定了时间范围参数，则根据时间范围设置起止时间
    if (StringUtils.isNotBlank(timeRange)) {
      Calendar calendar = Calendar.getInstance();
      Date now = calendar.getTime();
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      if (StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_DAY)) {
        // 今日
        startTime = calendar.getTime();
        endTime = now;
      } else if (StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_WEEK)) {
        // 本周
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        startTime = calendar.getTime();
        endTime = now;
      } else if (StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_MONTH)) {
        // 本月
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startTime = calendar.getTime();
        endTime = now;
      }
    }

    // 设置时间范围条件
    if (startTime != null) {
      queryWrapper.ge(PointsRecord::getCreateTime, startTime);
    }
    if (endTime != null) {
      queryWrapper.le(PointsRecord::getCreateTime, endTime);
    }

    // 排序
    String sortField = pointsQueryRequest.getSortField();
    String sortOrder = pointsQueryRequest.getSortOrder();
    if (StringUtils.isNotBlank(sortField)) {
      if ("createTime".equals(sortField)) {
        queryWrapper.orderBy(true, "asc".equals(sortOrder), PointsRecord::getCreateTime);
      } else if ("points".equals(sortField)) {
        queryWrapper.orderBy(true, "asc".equals(sortOrder), PointsRecord::getPoints);
      }
    } else {
      // 默认按创建时间倒序
      queryWrapper.orderByDesc(PointsRecord::getCreateTime);
    }

    // 执行分页查询
    Page<PointsRecord> page = new Page<>(current, pageSize);
    return pointsRecordMapper.selectPage(page, queryWrapper);
  }

  @Override
  public List<UserPointsRankVO> getPointsRanking(String timeRange, int count) {
    // 参数校验
    ThrowUtils.throwIf(
        count <= 0 || count > 100, new BusinessException(ErrorCode.FAIL, "获取数量应在1-100之间"));

    List<UserPointsRankVO> result = new ArrayList<>();

    // 根据时间范围获取排行榜
    if (StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_ALL)) {
      // 全部时间排行榜，直接查询用户总积分表
      QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
      queryWrapper.orderByDesc("totalPoints");
      queryWrapper.last("limit " + count);
      List<UserPoints> userPointsList = userPointsMapper.selectList(queryWrapper);

      // 转换为排行榜VO
      for (int i = 0; i < userPointsList.size(); i++) {
        UserPoints userPoints = userPointsList.get(i);
        User user = userService.getById(userPoints.getUserId());
        if (user != null) {
          UserPointsRankVO rankVO = new UserPointsRankVO();
          rankVO.setUserId(user.getId());
          rankVO.setUserName(user.getUserName());
          rankVO.setUserAvatar(user.getUserAvatar());
          rankVO.setTotalPoints(userPoints.getTotalPoints());
          rankVO.setRank(i + 1);
          result.add(rankVO);
        }
      }
    } else {
      // 特定时间范围排行榜，需要聚合积分记录
      Calendar calendar = Calendar.getInstance();
      Date now = calendar.getTime();
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      Date startTime = null;

      if (StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_DAY)) {
        // 今日
        startTime = calendar.getTime();
      } else if (StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_WEEK)) {
        // 本周
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        startTime = calendar.getTime();
      } else if (StringUtils.equals(timeRange, PointsConstant.TIME_RANGE_MONTH)) {
        // 本月
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startTime = calendar.getTime();
      }

      // 查询指定时间范围内的积分记录
      LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();
      if (startTime != null) {
        queryWrapper.ge(PointsRecord::getCreateTime, startTime);
      }
      queryWrapper.le(PointsRecord::getCreateTime, now);
      List<PointsRecord> allRecords = pointsRecordMapper.selectList(queryWrapper);

      // 按用户ID分组并计算总积分
      if (!allRecords.isEmpty()) {
        // 分组计算每个用户的积分总和
        Map<Long, Integer> userPointsMap =
            allRecords.stream()
                .collect(
                    Collectors.groupingBy(
                        PointsRecord::getUserId, Collectors.summingInt(PointsRecord::getPoints)));

        // 转换为排行榜列表并排序
        List<UserPointsRankVO> rankList =
            userPointsMap.entrySet().stream()
                .map(
                    entry -> {
                      User user = userService.getById(entry.getKey());
                      if (user == null) {
                        return null;
                      }
                      UserPointsRankVO rankVO = new UserPointsRankVO();
                      rankVO.setUserId(user.getId());
                      rankVO.setUserName(user.getUserName());
                      rankVO.setUserAvatar(user.getUserAvatar());
                      rankVO.setTotalPoints(entry.getValue());
                      return rankVO;
                    })
                .filter(vo -> vo != null)
                .sorted((vo1, vo2) -> vo2.getTotalPoints() - vo1.getTotalPoints())
                .limit(count)
                .collect(Collectors.toList());

        // 设置排名
        for (int i = 0; i < rankList.size(); i++) {
          rankList.get(i).setRank(i + 1);
        }

        result = rankList;
      }
    }

    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long addPointsRecord(
      Long userId, Integer points, String type, String description, Long relatedId) {
    // 校验参数
    ThrowUtils.throwIf(
        userId == null || userId <= 0, new BusinessException(ErrorCode.FAIL, "用户ID不能为空"));
    ThrowUtils.throwIf(points == null, new BusinessException(ErrorCode.FAIL, "积分值不能为空"));
    ThrowUtils.throwIf(type == null, new BusinessException(ErrorCode.FAIL, "积分类型不能为空"));
    ThrowUtils.throwIf(description == null, new BusinessException(ErrorCode.FAIL, "积分描述不能为空"));

    // 创建积分记录
    PointsRecord pointsRecord = new PointsRecord();
    pointsRecord.setUserId(userId);
    pointsRecord.setPoints(points);
    pointsRecord.setType(type);
    pointsRecord.setDescription(description);
    pointsRecord.setRelatedId(relatedId);
    pointsRecordMapper.insert(pointsRecord);

    // 更新用户总积分
    UserPoints userPoints = getUserPointsByUserId(userId);
    userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
    userPointsMapper.updateById(userPoints);

    return pointsRecord.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> userCheckin(Long userId) {
    // 校验用户是否存在
    userService.getById(userId);

    // 检查今日是否已签到
    if (hasCheckedInToday(userId)) {
      throw new BusinessException(ErrorCode.FAIL, "今日已签到");
    }

    // 创建签到记录
    UserCheckin userCheckin = new UserCheckin();
    userCheckin.setUserId(userId);
    userCheckin.setCheckinDate(new Date());
    userCheckinMapper.insert(userCheckin);

    // 添加积分记录
    Long recordId =
        addPointsRecord(
            userId,
            PointsConstant.CHECKIN_POINTS,
            PointsConstant.CHECKIN_TYPE,
            PointsConstant.CHECKIN_DESC,
            null);

    // 构建返回结果
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("points", PointsConstant.CHECKIN_POINTS);
    result.put("checkinDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    result.put("recordId", recordId);

    return result;
  }

  @Override
  public boolean hasCheckedInToday(Long userId) {
    // 获取今日日期（只保留年月日）
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date today = calendar.getTime();

    // 设置明天日期
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    Date tomorrow = calendar.getTime();

    // 查询今日是否有签到记录
    LambdaQueryWrapper<UserCheckin> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper
        .eq(UserCheckin::getUserId, userId)
        .ge(UserCheckin::getCheckinDate, today)
        .lt(UserCheckin::getCheckinDate, tomorrow);
    return userCheckinMapper.selectCount(queryWrapper) > 0;
  }

  @Override
  public Page<UserCheckin> getUserCheckinRecords(Long userId, long page, long pageSize) {
    // 参数校验
    if (userId == null || page <= 0 || pageSize <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 查询用户签到记录
    LambdaQueryWrapper<UserCheckin> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserCheckin::getUserId, userId);
    queryWrapper.orderByDesc(UserCheckin::getCheckinDate);

    // 执行分页查询
    Page<UserCheckin> checkinPage = new Page<>(page, pageSize);
    return userCheckinMapper.selectPage(checkinPage, queryWrapper);
  }

  @Override
  public List<Date> getUserMonthlyCheckins(Long userId) {
    // 获取当月第一天
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date firstDayOfMonth = calendar.getTime();

    // 获取下月第一天
    calendar.add(Calendar.MONTH, 1);
    Date firstDayOfNextMonth = calendar.getTime();

    // 查询当月签到记录
    LambdaQueryWrapper<UserCheckin> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper
        .eq(UserCheckin::getUserId, userId)
        .ge(UserCheckin::getCheckinDate, firstDayOfMonth)
        .lt(UserCheckin::getCheckinDate, firstDayOfNextMonth)
        .select(UserCheckin::getCheckinDate);
    List<UserCheckin> checkins = userCheckinMapper.selectList(queryWrapper);

    // 提取签到日期
    return checkins.stream().map(UserCheckin::getCheckinDate).collect(Collectors.toList());
  }

  /**
   * 获取用户积分信息，如果不存在则创建
   *
   * @param userId 用户ID
   * @return 用户积分信息
   */
  private UserPoints getUserPointsByUserId(Long userId) {
    // 查询用户积分
    LambdaQueryWrapper<UserPoints> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserPoints::getUserId, userId);
    UserPoints userPoints = userPointsMapper.selectOne(queryWrapper);

    // 如果不存在则创建
    if (userPoints == null) {
      userPoints = new UserPoints();
      userPoints.setUserId(userId);
      userPoints.setTotalPoints(0);
      userPointsMapper.insert(userPoints);
    }

    return userPoints;
  }
}
