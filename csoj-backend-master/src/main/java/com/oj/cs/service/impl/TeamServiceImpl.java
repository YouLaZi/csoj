package com.oj.cs.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.mapper.TeamMapper;
import com.oj.cs.mapper.TeamMemberMapper;
import com.oj.cs.mapper.UserMapper;
import com.oj.cs.model.dto.team.*;
import com.oj.cs.model.entity.Team;
import com.oj.cs.model.entity.TeamMember;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.TeamVO;
import com.oj.cs.model.vo.UserVO;
import com.oj.cs.service.TeamService;
import com.oj.cs.service.UserService;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

/** 团队服务实现 */
@Slf4j
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

  @Resource private TeamMemberMapper teamMemberMapper;

  @Resource private UserMapper userMapper;

  @Resource private UserService userService;

  /** 用户最多加入的团队数量 */
  private static final int MAX_TEAM_PER_USER = 5;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public long createTeam(TeamCreateRequest request, HttpServletRequest httpRequest) {
    // 1. 校验参数
    if (request == null || StringUtils.isBlank(request.getName())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "团队名称不能为空");
    }

    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    // 2. 检查用户已加入的团队数量
    int teamCount = teamMemberMapper.countByUserId(userId);
    if (teamCount >= MAX_TEAM_PER_USER) {
      throw new BusinessException(
          ErrorCode.OPERATION_ERROR, "最多只能加入 " + MAX_TEAM_PER_USER + " 个团队");
    }

    // 3. 检查团队名是否重复
    QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("name", request.getName());
    long count = this.count(queryWrapper);
    if (count > 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "团队名称已存在");
    }

    // 4. 创建团队
    Team team = new Team();
    BeanUtils.copyProperties(request, team);
    team.setLeaderId(userId);
    team.setInviteCode(generateUniqueInviteCode());
    team.setMaxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 5);
    team.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : 1);
    team.setTotalScore(0L);
    team.setWinCount(0);
    team.setLoseCount(0);
    team.setDrawCount(0);
    team.setRating(1000);

    boolean saved = this.save(team);
    ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "创建团队失败");

    // 5. 添加队长为团队成员
    TeamMember teamMember = new TeamMember();
    teamMember.setTeamId(team.getId());
    teamMember.setUserId(userId);
    teamMember.setRole("leader");
    teamMember.setContributionScore(0);
    teamMember.setSolvedCount(0);
    teamMember.setContestCount(0);

    int inserted = teamMemberMapper.insert(teamMember);
    ThrowUtils.throwIf(inserted <= 0, ErrorCode.OPERATION_ERROR, "添加团队成员失败");

    log.info("用户 {} 创建团队 {} 成功", userId, team.getId());
    return team.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateTeam(TeamUpdateRequest request, HttpServletRequest httpRequest) {
    if (request == null || request.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    User loginUser = userService.getLoginUser(httpRequest);
    long teamId = request.getId();

    // 检查权限
    TeamMember teamMember = getTeamMember(teamId, loginUser.getId());
    if (teamMember == null || !"leader".equals(teamMember.getRole())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有队长可以修改团队信息");
    }

    // 检查团队名是否重复
    if (StringUtils.isNotBlank(request.getName())) {
      QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("name", request.getName());
      queryWrapper.ne("id", teamId);
      long count = this.count(queryWrapper);
      if (count > 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "团队名称已存在");
      }
    }

    Team team = new Team();
    BeanUtils.copyProperties(request, team);
    return this.updateById(team);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean deleteTeam(long teamId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);

    // 检查权限
    TeamMember teamMember = getTeamMember(teamId, loginUser.getId());
    if (teamMember == null || !"leader".equals(teamMember.getRole())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有队长可以解散团队");
    }

    // 删除团队成员记录
    QueryWrapper<TeamMember> memberQueryWrapper = new QueryWrapper<>();
    memberQueryWrapper.eq("team_id", teamId);
    teamMemberMapper.delete(memberQueryWrapper);

    // 删除团队
    return this.removeById(teamId);
  }

  @Override
  public TeamVO getTeamById(long teamId, HttpServletRequest httpRequest) {
    Team team = this.getById(teamId);
    if (team == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "团队不存在");
    }

    User loginUser = userService.getLoginUserPermitNull(httpRequest);
    return convertToTeamVO(team, loginUser);
  }

  @Override
  public Page<TeamVO> listTeams(TeamQueryRequest request, HttpServletRequest httpRequest) {
    if (request == null) {
      request = new TeamQueryRequest();
    }

    QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

    // 名称模糊搜索
    if (StringUtils.isNotBlank(request.getName())) {
      queryWrapper.like("name", request.getName());
    }

    // 公开招募筛选
    if (request.getIsPublic() != null) {
      queryWrapper.eq("is_public", request.getIsPublic());
    }

    // 积分范围
    if (request.getMinRating() != null) {
      queryWrapper.ge("rating", request.getMinRating());
    }
    if (request.getMaxRating() != null) {
      queryWrapper.le("rating", request.getMaxRating());
    }

    // 按积分排序
    queryWrapper.orderByDesc("rating");

    Page<Team> page =
        this.page(new Page<>(request.getCurrent(), request.getPageSize()), queryWrapper);

    User loginUser = userService.getLoginUserPermitNull(httpRequest);
    List<TeamVO> teamVOList =
        page.getRecords().stream()
            .map(team -> convertToTeamVO(team, loginUser))
            .collect(Collectors.toList());

    Page<TeamVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resultPage.setRecords(teamVOList);
    return resultPage;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean joinTeam(JoinTeamRequest request, HttpServletRequest httpRequest) {
    if (request == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    // 查找团队
    Team team;
    if (request.getTeamId() != null) {
      team = this.getById(request.getTeamId());
    } else if (StringUtils.isNotBlank(request.getInviteCode())) {
      QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("invite_code", request.getInviteCode());
      team = this.getOne(queryWrapper);
    } else {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "请提供团队ID或邀请码");
    }

    if (team == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "团队不存在");
    }

    // 检查是否公开招募
    if (team.getIsPublic() != 1 && StringUtils.isBlank(request.getInviteCode())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "该团队不开放公开加入");
    }

    // 检查邀请码
    if (StringUtils.isNotBlank(request.getInviteCode())
        && !request.getInviteCode().equals(team.getInviteCode())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "邀请码无效");
    }

    // 检查用户已加入的团队数量
    int teamCount = teamMemberMapper.countByUserId(userId);
    if (teamCount >= MAX_TEAM_PER_USER) {
      throw new BusinessException(
          ErrorCode.OPERATION_ERROR, "最多只能加入 " + MAX_TEAM_PER_USER + " 个团队");
    }

    // 检查是否已加入
    TeamMember existingMember = getTeamMember(team.getId(), userId);
    if (existingMember != null) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经是该团队成员");
    }

    // 检查团队人数
    int currentMembers = teamMemberMapper.countByTeamId(team.getId());
    if (currentMembers >= team.getMaxMembers()) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "团队人数已满");
    }

    // 加入团队
    TeamMember teamMember = new TeamMember();
    teamMember.setTeamId(team.getId());
    teamMember.setUserId(userId);
    teamMember.setRole("member");
    teamMember.setContributionScore(0);
    teamMember.setSolvedCount(0);
    teamMember.setContestCount(0);

    int inserted = teamMemberMapper.insert(teamMember);
    ThrowUtils.throwIf(inserted <= 0, ErrorCode.OPERATION_ERROR, "加入团队失败");

    log.info("用户 {} 加入团队 {} 成功", userId, team.getId());
    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean quitTeam(long teamId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    TeamMember teamMember = getTeamMember(teamId, userId);
    if (teamMember == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "您不是该团队成员");
    }

    // 队长不能退出，只能解散或转让
    if ("leader".equals(teamMember.getRole())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "队长不能退出团队，请先转让队长或解散团队");
    }

    int deleted = teamMemberMapper.deleteById(teamMember.getId());
    return deleted > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean kickMember(long teamId, long userId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);

    // 检查权限
    TeamMember operatorMember = getTeamMember(teamId, loginUser.getId());
    if (operatorMember == null || !"leader".equals(operatorMember.getRole())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有队长可以移除成员");
    }

    TeamMember targetMember = getTeamMember(teamId, userId);
    if (targetMember == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户不是团队成员");
    }

    // 不能移除队长
    if ("leader".equals(targetMember.getRole())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能移除队长");
    }

    int deleted = teamMemberMapper.deleteById(targetMember.getId());
    return deleted > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean transferLeader(long teamId, long newLeaderId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    // 检查当前用户是否为队长
    TeamMember currentLeader = getTeamMember(teamId, userId);
    if (currentLeader == null || !"leader".equals(currentLeader.getRole())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有队长可以转让队长");
    }

    // 检查新队长是否为团队成员
    TeamMember newLeader = getTeamMember(teamId, newLeaderId);
    if (newLeader == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户不是团队成员");
    }

    // 更新角色
    currentLeader.setRole("member");
    newLeader.setRole("leader");
    teamMemberMapper.updateById(currentLeader);
    teamMemberMapper.updateById(newLeader);

    // 更新团队队长
    Team team = this.getById(teamId);
    team.setLeaderId(newLeaderId);
    this.updateById(team);

    log.info("团队 {} 队长由 {} 转让给 {}", teamId, userId, newLeaderId);
    return true;
  }

  @Override
  public String generateInviteCode(long teamId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);

    TeamMember teamMember = getTeamMember(teamId, loginUser.getId());
    if (teamMember == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "您不是该团队成员");
    }

    Team team = this.getById(teamId);
    if (team == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "团队不存在");
    }

    // 生成新的邀请码
    String newInviteCode = generateUniqueInviteCode();
    team.setInviteCode(newInviteCode);
    this.updateById(team);

    return newInviteCode;
  }

  @Override
  public List<TeamVO> getMyTeams(HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    List<Long> teamIds = teamMemberMapper.selectTeamIdsByUserId(userId);
    if (teamIds.isEmpty()) {
      return new ArrayList<>();
    }

    List<Team> teams = this.listByIds(teamIds);
    return teams.stream()
        .map(team -> convertToTeamVO(team, loginUser))
        .collect(Collectors.toList());
  }

  @Override
  public Page<TeamVO> getTeamRanking(TeamQueryRequest request) {
    if (request == null) {
      request = new TeamQueryRequest();
    }

    QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
    queryWrapper.orderByDesc("rating");

    Page<Team> page =
        this.page(new Page<>(request.getCurrent(), request.getPageSize()), queryWrapper);

    List<TeamVO> teamVOList =
        page.getRecords().stream()
            .map(team -> convertToTeamVO(team, null))
            .collect(Collectors.toList());

    // 设置排名
    int rank = (int) ((page.getCurrent() - 1) * page.getSize());
    for (TeamVO teamVO : teamVOList) {
      teamVO.setRank(++rank);
    }

    Page<TeamVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resultPage.setRecords(teamVOList);
    return resultPage;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateTeamRecord(long teamId, boolean isWin, boolean isDraw) {
    Team team = this.getById(teamId);
    if (team == null) {
      return;
    }

    if (isDraw) {
      team.setDrawCount(team.getDrawCount() + 1);
    } else if (isWin) {
      team.setWinCount(team.getWinCount() + 1);
    } else {
      team.setLoseCount(team.getLoseCount() + 1);
    }

    this.updateById(team);
  }

  /** 获取团队成员记录 */
  private TeamMember getTeamMember(long teamId, long userId) {
    QueryWrapper<TeamMember> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("team_id", teamId);
    queryWrapper.eq("user_id", userId);
    return teamMemberMapper.selectOne(queryWrapper);
  }

  /** 生成唯一邀请码 */
  private String generateUniqueInviteCode() {
    String inviteCode;
    do {
      inviteCode = RandomUtil.randomString(8).toUpperCase();
    } while (isInviteCodeExists(inviteCode));
    return inviteCode;
  }

  /** 检查邀请码是否存在 */
  private boolean isInviteCodeExists(String inviteCode) {
    QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("invite_code", inviteCode);
    return this.count(queryWrapper) > 0;
  }

  /** 转换为 TeamVO */
  private TeamVO convertToTeamVO(Team team, User loginUser) {
    if (team == null) {
      return null;
    }

    TeamVO teamVO = new TeamVO();
    BeanUtils.copyProperties(team, teamVO);

    // 获取成员数量
    int memberCount = teamMemberMapper.countByTeamId(team.getId());
    teamVO.setCurrentMembers(memberCount);

    // 获取队长信息
    User leader = userMapper.selectById(team.getLeaderId());
    if (leader != null) {
      UserVO leaderVO = new UserVO();
      BeanUtils.copyProperties(leader, leaderVO);
      teamVO.setLeader(leaderVO);
    }

    // 获取排名
    int rank = baseMapper.countByRatingGreaterThan(team.getRating()) + 1;
    teamVO.setRank(rank);

    // 设置当前用户的加入状态和角色
    if (loginUser != null) {
      TeamMember teamMember = getTeamMember(team.getId(), loginUser.getId());
      teamVO.setHasJoined(teamMember != null);
      if (teamMember != null) {
        teamVO.setUserRole(teamMember.getRole());
      }
    }

    return teamVO;
  }
}
