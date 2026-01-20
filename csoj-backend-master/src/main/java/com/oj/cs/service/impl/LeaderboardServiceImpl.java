package com.oj.cs.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oj.cs.constant.PointsConstant;
import com.oj.cs.mapper.PointsRecordMapper;
import com.oj.cs.mapper.UserPointsMapper;
import com.oj.cs.model.entity.PointsRecord;
import com.oj.cs.model.entity.UserPoints;
import com.oj.cs.model.vo.LeaderboardUserVO;
import com.oj.cs.model.vo.LeaderboardVO;
import com.oj.cs.service.LeaderboardService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 排行榜服务实现 */
@Service
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

  @Resource private UserPointsMapper userPointsMapper;

  @Resource private PointsRecordMapper pointsRecordMapper;

  @Resource private UserService userService;

  @Override
  public LeaderboardVO getPointsLeaderboard(String timeRange) {
    // 默认为周榜
    if (timeRange == null) {
      timeRange = PointsConstant.TIME_RANGE_WEEK;
    }

    // 根据时间范围获取排行榜数据
    List<LeaderboardUserVO> leaderboardUsers;
    switch (timeRange) {
      case PointsConstant.TIME_RANGE_DAY:
        leaderboardUsers = getDayLeaderboard();
        break;
      case PointsConstant.TIME_RANGE_WEEK:
        leaderboardUsers = getWeekLeaderboard();
        break;
      case PointsConstant.TIME_RANGE_MONTH:
        leaderboardUsers = getMonthLeaderboard();
        break;
      case PointsConstant.TIME_RANGE_ALL:
      default:
        leaderboardUsers = getAllTimeLeaderboard();
        break;
    }

    // 设置排名
    for (int i = 0; i < leaderboardUsers.size(); i++) {
      leaderboardUsers.get(i).setRank(i + 1);
    }

    // 组装VO
    LeaderboardVO leaderboardVO = new LeaderboardVO();
    leaderboardVO.setTimeRange(timeRange);
    leaderboardVO.setUsers(leaderboardUsers);

    return leaderboardVO;
  }

  /** 获取日榜 */
  private List<LeaderboardUserVO> getDayLeaderboard() {
    // 获取今日开始时间
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date startTime = calendar.getTime();

    // 查询今日积分记录
    LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.ge(PointsRecord::getCreateTime, startTime);
    List<PointsRecord> records = pointsRecordMapper.selectList(queryWrapper);

    // 按用户ID分组，计算每个用户的积分总和
    Map<Long, Integer> userPointsMap =
        records.stream()
            .collect(
                Collectors.groupingBy(
                    PointsRecord::getUserId, Collectors.summingInt(PointsRecord::getPoints)));

    // 转换为排行榜用户列表并排序
    return convertToLeaderboardUsers(userPointsMap);
  }

  /** 获取周榜 */
  private List<LeaderboardUserVO> getWeekLeaderboard() {
    // 获取本周开始时间（周一）
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date startTime = calendar.getTime();

    // 查询本周积分记录
    LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.ge(PointsRecord::getCreateTime, startTime);
    List<PointsRecord> records = pointsRecordMapper.selectList(queryWrapper);

    // 按用户ID分组，计算每个用户的积分总和
    Map<Long, Integer> userPointsMap =
        records.stream()
            .collect(
                Collectors.groupingBy(
                    PointsRecord::getUserId, Collectors.summingInt(PointsRecord::getPoints)));

    // 转换为排行榜用户列表并排序
    return convertToLeaderboardUsers(userPointsMap);
  }

  /** 获取月榜 */
  private List<LeaderboardUserVO> getMonthLeaderboard() {
    // 获取本月开始时间
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date startTime = calendar.getTime();

    // 查询本月积分记录
    LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.ge(PointsRecord::getCreateTime, startTime);
    List<PointsRecord> records = pointsRecordMapper.selectList(queryWrapper);

    // 按用户ID分组，计算每个用户的积分总和
    Map<Long, Integer> userPointsMap =
        records.stream()
            .collect(
                Collectors.groupingBy(
                    PointsRecord::getUserId, Collectors.summingInt(PointsRecord::getPoints)));

    // 转换为排行榜用户列表并排序
    return convertToLeaderboardUsers(userPointsMap);
  }

  /** 获取总榜 */
  private List<LeaderboardUserVO> getAllTimeLeaderboard() {
    // 查询所有用户积分
    List<UserPoints> userPointsList = userPointsMapper.selectList(null);

    // 转换为Map
    Map<Long, Integer> userPointsMap =
        userPointsList.stream()
            .collect(Collectors.toMap(UserPoints::getUserId, UserPoints::getTotalPoints));

    // 转换为排行榜用户列表并排序
    return convertToLeaderboardUsers(userPointsMap);
  }

  /** 将用户积分Map转换为排行榜用户列表 */
  private List<LeaderboardUserVO> convertToLeaderboardUsers(Map<Long, Integer> userPointsMap) {
    List<LeaderboardUserVO> result = new ArrayList<>();

    // 遍历用户积分Map
    userPointsMap.forEach(
        (userId, points) -> {
          // 查询用户信息
          try {
            String userName = userService.getById(userId).getUserName();

            // 创建排行榜用户VO
            LeaderboardUserVO leaderboardUserVO = new LeaderboardUserVO();
            leaderboardUserVO.setUserId(userId);
            leaderboardUserVO.setUserName(userName);
            leaderboardUserVO.setPoints(points);

            result.add(leaderboardUserVO);
          } catch (Exception e) {
            log.error("获取用户信息失败，userId={}", userId, e);
          }
        });

    // 按积分降序排序
    result.sort((a, b) -> b.getPoints() - a.getPoints());

    return result;
  }
}
