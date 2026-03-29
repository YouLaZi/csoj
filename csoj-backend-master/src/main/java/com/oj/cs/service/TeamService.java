package com.oj.cs.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.team.*;
import com.oj.cs.model.entity.Team;
import com.oj.cs.model.vo.TeamVO;

/** 团队服务接口 */
public interface TeamService extends IService<Team> {

  /** 创建团队 */
  long createTeam(TeamCreateRequest request, HttpServletRequest httpRequest);

  /** 更新团队 */
  boolean updateTeam(TeamUpdateRequest request, HttpServletRequest httpRequest);

  /** 解散团队 */
  boolean deleteTeam(long teamId, HttpServletRequest httpRequest);

  /** 获取团队详情 */
  TeamVO getTeamById(long teamId, HttpServletRequest httpRequest);

  /** 分页查询团队列表 */
  com.baomidou.mybatisplus.extension.plugins.pagination.Page<TeamVO> listTeams(
      TeamQueryRequest request, HttpServletRequest httpRequest);

  /** 加入团队 */
  boolean joinTeam(JoinTeamRequest request, HttpServletRequest httpRequest);

  /** 退出团队 */
  boolean quitTeam(long teamId, HttpServletRequest httpRequest);

  /** 移除成员 */
  boolean kickMember(long teamId, long userId, HttpServletRequest httpRequest);

  /** 转让队长 */
  boolean transferLeader(long teamId, long newLeaderId, HttpServletRequest httpRequest);

  /** 生成邀请码 */
  String generateInviteCode(long teamId, HttpServletRequest httpRequest);

  /** 获取当前用户的团队列表 */
  List<TeamVO> getMyTeams(HttpServletRequest httpRequest);

  /** 获取团队排行榜 */
  com.baomidou.mybatisplus.extension.plugins.pagination.Page<TeamVO> getTeamRanking(
      TeamQueryRequest request);

  /** 更新团队战绩 */
  void updateTeamRecord(long teamId, boolean isWin, boolean isDraw);
}
