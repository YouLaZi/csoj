package com.oj.cs.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.model.dto.competition.*;
import com.oj.cs.model.vo.CompetitionVO;
import com.oj.cs.model.vo.MatchVO;
import com.oj.cs.model.vo.TeamVO;

/** 比赛服务接口 */
public interface CompetitionService {

  /**
   * 创建比赛
   *
   * @param request 比赛创建请求
   * @param httpRequest HTTP请求
   * @return 比赛ID
   */
  long createCompetition(CompetitionCreateRequest request, HttpServletRequest httpRequest);

  /**
   * 更新比赛信息
   *
   * @param request 比赛更新请求
   * @param httpRequest HTTP请求
   * @return 是否成功
   */
  boolean updateCompetition(CompetitionUpdateRequest request, HttpServletRequest httpRequest);

  /**
   * 删除比赛
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 是否成功
   */
  boolean deleteCompetition(long competitionId, HttpServletRequest httpRequest);

  /**
   * 获取比赛详情
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 比赛详情
   */
  CompetitionVO getCompetitionById(long competitionId, HttpServletRequest httpRequest);

  /**
   * 分页查询比赛列表
   *
   * @param request 查询请求
   * @param httpRequest HTTP请求
   * @return 比赛分页列表
   */
  Page<CompetitionVO> listCompetitions(
      CompetitionQueryRequest request, HttpServletRequest httpRequest);

  /**
   * 报名参加比赛
   *
   * @param request 报名请求
   * @param httpRequest HTTP请求
   * @return 是否成功
   */
  boolean registerCompetition(CompetitionRegisterRequest request, HttpServletRequest httpRequest);

  /**
   * 取消报名
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 是否成功
   */
  boolean cancelRegistration(long competitionId, HttpServletRequest httpRequest);

  /**
   * 获取比赛报名列表
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 报名团队列表
   */
  List<TeamVO> getRegistrations(long competitionId, HttpServletRequest httpRequest);

  /**
   * 开始比赛
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 是否成功
   */
  boolean startCompetition(long competitionId, HttpServletRequest httpRequest);

  /**
   * 结束比赛
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 是否成功
   */
  boolean endCompetition(long competitionId, HttpServletRequest httpRequest);

  /**
   * 生成比赛对阵
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 是否成功
   */
  boolean generateMatches(long competitionId, HttpServletRequest httpRequest);

  /**
   * 获取比赛对阵表
   *
   * @param competitionId 比赛ID
   * @param httpRequest HTTP请求
   * @return 对阵表
   */
  List<MatchVO> getMatchList(long competitionId, HttpServletRequest httpRequest);

  /**
   * 获取我报名的比赛
   *
   * @param httpRequest HTTP请求
   * @return 比赛列表
   */
  List<CompetitionVO> getMyCompetitions(HttpServletRequest httpRequest);

  /**
   * 获取进行中的比赛
   *
   * @return 比赛列表
   */
  List<CompetitionVO> getOngoingCompetitions();
}
