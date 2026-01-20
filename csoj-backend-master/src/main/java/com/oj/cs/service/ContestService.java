package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.contest.ContestAddRequest;
import com.oj.cs.model.dto.contest.ContestJoinRequest;
import com.oj.cs.model.dto.contest.ContestQueryRequest;
import com.oj.cs.model.dto.contest.ContestUpdateRequest;
import com.oj.cs.model.entity.Contest;
import com.oj.cs.model.vo.ContestRankingVO;
import com.oj.cs.model.vo.ContestStatisticsVO;
import com.oj.cs.model.vo.ContestVO;

/** 比赛服务 */
public interface ContestService extends IService<Contest> {

  /**
   * 创建比赛
   *
   * @param contestAddRequest 创建请求
   * @param userId 创建用户ID
   * @return 比赛ID
   */
  Long createContest(ContestAddRequest contestAddRequest, Long userId);

  /**
   * 更新比赛
   *
   * @param contestUpdateRequest 更新请求
   * @param userId 操作用户ID
   * @return 是否成功
   */
  Boolean updateContest(ContestUpdateRequest contestUpdateRequest, Long userId);

  /**
   * 删除比赛
   *
   * @param id 比赛ID
   * @param userId 操作用户ID
   * @return 是否成功
   */
  Boolean deleteContest(Long id, Long userId);

  /**
   * 参加比赛
   *
   * @param joinRequest 参加请求
   * @param userId 用户ID
   * @return 是否成功
   */
  Boolean joinContest(ContestJoinRequest joinRequest, Long userId);

  /**
   * 获取比赛排行榜
   *
   * @param contestId 比赛ID
   * @param userId 查询用户ID
   * @return 排行榜列表
   */
  List<ContestRankingVO> getContestRanking(Long contestId, Long userId);

  /**
   * 根据ID获取比赛VO
   *
   * @param id 比赛ID
   * @param userId 查询用户ID
   * @return 比赛VO
   */
  ContestVO getContestVOById(Long id, Long userId);

  /**
   * 分页查询比赛
   *
   * @param current 当前页
   * @param size 每页大小
   * @param queryRequest 查询条件
   * @param userId 查询用户ID
   * @param isAdmin 是否管理员
   * @return 分页结果
   */
  Page<ContestVO> listContestByPage(
      long current, long size, ContestQueryRequest queryRequest, Long userId, boolean isAdmin);

  /**
   * 实体转VO
   *
   * @param contest 比赛实体
   * @return 比赛VO
   */
  ContestVO getContestVO(Contest contest);

  /**
   * 实体列表转VO列表
   *
   * @param contestList 比赛实体列表
   * @return 比赛VO列表
   */
  List<ContestVO> getContestVO(List<Contest> contestList);

  /**
   * 构建查询条件
   *
   * @param queryRequest 查询条件
   * @return QueryWrapper
   */
  QueryWrapper<Contest> getQueryWrapper(ContestQueryRequest queryRequest);

  /** 更新比赛状态（定时任务调用） */
  void updateContestStatus();

  /**
   * 获取比赛统计数据
   *
   * @param contestId 比赛ID
   * @return 统计数据
   */
  ContestStatisticsVO getContestStatistics(Long contestId);
}
