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
import com.oj.cs.mapper.*;
import com.oj.cs.model.dto.competition.*;
import com.oj.cs.model.entity.*;
import com.oj.cs.model.vo.*;
import com.oj.cs.service.CompetitionService;
import com.oj.cs.service.TeamService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 比赛服务实现 */
@Slf4j
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition>
    implements CompetitionService {

  @Resource private CompetitionRegistrationMapper registrationMapper;
  @Resource private CompetitionMatchMapper matchMapper;
  @Resource private TeamMapper teamMapper;
  @Resource private TeamMemberMapper teamMemberMapper;
  @Resource private UserMapper userMapper;
  @Resource private UserService userService;
  @Resource private TeamService teamService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public long createCompetition(CompetitionCreateRequest request, HttpServletRequest httpRequest) {
    // 1. 校验参数
    if (request == null || StringUtils.isBlank(request.getName())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛名称不能为空");
    }

    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    // 2. 校验时间
    if (request.getRegisterStartTime() != null && request.getRegisterEndTime() != null) {
      if (request.getRegisterStartTime().after(request.getRegisterEndTime())) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "报名开始时间不能晚于结束时间");
      }
    }
    if (request.getStartTime() != null && request.getEndTime() != null) {
      if (request.getStartTime().after(request.getEndTime())) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛开始时间不能晚于结束时间");
      }
    }

    // 3. 创建比赛
    Competition competition = new Competition();
    BeanUtils.copyProperties(request, competition);
    competition.setCreatorId(userId);
    competition.setStatus(0); // 未开始
    competition.setCurrentTeams(0);

    boolean saved = this.save(competition);
    ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "创建比赛失败");

    log.info("用户 {} 创建比赛 {} 成功", userId, competition.getId());
    return competition.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updateCompetition(
      CompetitionUpdateRequest request, HttpServletRequest httpRequest) {
    if (request == null || request.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    User loginUser = userService.getLoginUser(httpRequest);
    long competitionId = request.getId();

    // 检查权限
    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }
    if (!competition.getCreatorId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有创建者可以修改比赛信息");
    }

    // 比赛进行中或已结束不能修改
    if (competition.getStatus() >= 2) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛进行中或已结束，无法修改");
    }

    Competition updateCompetition = new Competition();
    BeanUtils.copyProperties(request, updateCompetition);
    return this.updateById(updateCompetition);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean deleteCompetition(long competitionId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);

    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }
    if (!competition.getCreatorId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有创建者可以删除比赛");
    }

    // 比赛进行中不能删除
    if (competition.getStatus() == 2) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛进行中，无法删除");
    }

    return this.removeById(competitionId);
  }

  @Override
  public CompetitionVO getCompetitionById(long competitionId, HttpServletRequest httpRequest) {
    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }

    User loginUser = userService.getLoginUserPermitNull(httpRequest);
    return convertToCompetitionVO(competition, loginUser);
  }

  @Override
  public Page<CompetitionVO> listCompetitions(
      CompetitionQueryRequest request, HttpServletRequest httpRequest) {
    if (request == null) {
      request = new CompetitionQueryRequest();
    }

    QueryWrapper<Competition> queryWrapper = new QueryWrapper<>();

    // 名称模糊搜索
    if (StringUtils.isNotBlank(request.getName())) {
      queryWrapper.like("name", request.getName());
    }

    // 类型筛选
    if (StringUtils.isNotBlank(request.getType())) {
      queryWrapper.eq("type", request.getType());
    }

    // 状态筛选
    if (request.getStatus() != null) {
      queryWrapper.eq("status", request.getStatus());
    }

    // 公开筛选
    if (request.getIsPublic() != null) {
      queryWrapper.eq("is_public", request.getIsPublic());
    }

    // 按开始时间倒序
    queryWrapper.orderByDesc("start_time");

    Page<Competition> page =
        this.page(new Page<>(request.getCurrent(), request.getPageSize()), queryWrapper);

    User loginUser = userService.getLoginUserPermitNull(httpRequest);
    List<CompetitionVO> competitionVOList =
        page.getRecords().stream()
            .map(comp -> convertToCompetitionVO(comp, loginUser))
            .collect(Collectors.toList());

    Page<CompetitionVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resultPage.setRecords(competitionVOList);
    return resultPage;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean registerCompetition(
      CompetitionRegisterRequest request, HttpServletRequest httpRequest) {
    if (request == null || request.getCompetitionId() == null || request.getTeamId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();
    long competitionId = request.getCompetitionId();
    long teamId = request.getTeamId();

    // 检查比赛是否存在
    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }

    // 检查比赛状态
    if (competition.getStatus() != 1) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛不在报名阶段");
    }

    // 检查报名时间
    Date now = new Date();
    if (competition.getRegisterStartTime() != null
        && now.before(competition.getRegisterStartTime())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "报名尚未开始");
    }
    if (competition.getRegisterEndTime() != null && now.after(competition.getRegisterEndTime())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "报名已结束");
    }

    // 检查用户是否为团队成员
    TeamMember teamMember = getTeamMember(teamId, userId);
    if (teamMember == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "您不是该团队成员");
    }

    // 检查团队人数
    int teamSize = teamMemberMapper.countByTeamId(teamId);
    if (competition.getMinTeamSize() != null && teamSize < competition.getMinTeamSize()) {
      throw new BusinessException(
          ErrorCode.OPERATION_ERROR, "团队人数不足 " + competition.getMinTeamSize() + " 人");
    }
    if (competition.getMaxTeamSize() != null && teamSize > competition.getMaxTeamSize()) {
      throw new BusinessException(
          ErrorCode.OPERATION_ERROR, "团队人数超过 " + competition.getMaxTeamSize() + " 人");
    }

    // 检查是否已报名
    int exists = registrationMapper.countByCompetitionIdAndTeamId(competitionId, teamId);
    if (exists > 0) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "该团队已报名");
    }

    // 检查比赛人数限制
    int currentTeams = baseMapper.countRegistrationsByCompetitionId(competitionId);
    if (competition.getMaxTeams() != null && currentTeams >= competition.getMaxTeams()) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "报名队伍已满");
    }

    // 创建报名记录
    CompetitionRegistration registration = new CompetitionRegistration();
    registration.setCompetitionId(competitionId);
    registration.setTeamId(teamId);
    registration.setRegisterTime(now);
    registration.setStatus(1); // 已确认

    int inserted = registrationMapper.insert(registration);
    ThrowUtils.throwIf(inserted <= 0, ErrorCode.OPERATION_ERROR, "报名失败");

    // 更新比赛当前队伍数
    competition.setCurrentTeams(currentTeams + 1);
    this.updateById(competition);

    log.info("用户 {} 代表团队 {} 报名比赛 {} 成功", userId, teamId, competitionId);
    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean cancelRegistration(long competitionId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }

    // 检查比赛状态
    if (competition.getStatus() >= 2) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛已开始，无法取消报名");
    }

    // 查找用户报名的团队
    List<Long> userTeamIds = teamMemberMapper.selectTeamIdsByUserId(userId);
    CompetitionRegistration registration = null;
    for (Long teamId : userTeamIds) {
      registration = registrationMapper.selectByCompetitionIdAndTeamId(competitionId, teamId);
      if (registration != null) {
        break;
      }
    }

    if (registration == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "您未报名该比赛");
    }

    // 删除报名记录
    int deleted = registrationMapper.deleteById(registration.getId());
    if (deleted > 0) {
      // 更新比赛当前队伍数
      int currentTeams = baseMapper.countRegistrationsByCompetitionId(competitionId);
      competition.setCurrentTeams(currentTeams);
      this.updateById(competition);
    }

    return deleted > 0;
  }

  @Override
  public List<TeamVO> getRegistrations(long competitionId, HttpServletRequest httpRequest) {
    List<Long> teamIds = registrationMapper.selectTeamIdsByCompetitionId(competitionId);
    if (teamIds.isEmpty()) {
      return new ArrayList<>();
    }

    List<Team> teams = teamMapper.selectBatchIds(teamIds);
    return teams.stream()
        .map(
            team -> {
              TeamVO teamVO = new TeamVO();
              BeanUtils.copyProperties(team, teamVO);
              int memberCount = teamMemberMapper.countByTeamId(team.getId());
              teamVO.setCurrentMembers(memberCount);
              return teamVO;
            })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean startCompetition(long competitionId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);

    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }
    if (!competition.getCreatorId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有创建者可以开始比赛");
    }

    if (competition.getStatus() != 1) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛不在报名阶段，无法开始");
    }

    // 更新状态为进行中
    int updated = baseMapper.updateStatus(competitionId, 2);
    if (updated > 0) {
      log.info("比赛 {} 已开始", competitionId);
    }
    return updated > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean endCompetition(long competitionId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);

    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }
    if (!competition.getCreatorId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有创建者可以结束比赛");
    }

    if (competition.getStatus() != 2) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛未在进行中");
    }

    // 更新状态为已结束
    int updated = baseMapper.updateStatus(competitionId, 3);
    if (updated > 0) {
      log.info("比赛 {} 已结束", competitionId);
    }
    return updated > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean generateMatches(long competitionId, HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);

    Competition competition = this.getById(competitionId);
    if (competition == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
    }
    if (!competition.getCreatorId().equals(loginUser.getId())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有创建者可以生成对阵");
    }

    // 获取报名团队
    List<Long> teamIds = registrationMapper.selectTeamIdsByCompetitionId(competitionId);
    if (teamIds.size() < 2) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "报名团队不足，无法生成对阵");
    }

    // 根据比赛类型生成对阵
    String type = competition.getType();
    switch (type) {
      case "elimination":
        generateEliminationMatches(competitionId, teamIds);
        break;
      case "round_robin":
        generateRoundRobinMatches(competitionId, teamIds);
        break;
      case "team_ac":
        generateTeamACMatches(competitionId, teamIds);
        break;
      default:
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的比赛类型");
    }

    // 如果比赛未开始，自动进入报名阶段
    if (competition.getStatus() == 0) {
      baseMapper.updateStatus(competitionId, 1);
    }

    log.info("比赛 {} 对阵已生成", competitionId);
    return true;
  }

  @Override
  public List<MatchVO> getMatchList(long competitionId, HttpServletRequest httpRequest) {
    List<CompetitionMatch> matches = matchMapper.selectByCompetitionId(competitionId);
    return matches.stream().map(this::convertToMatchVO).collect(Collectors.toList());
  }

  @Override
  public List<CompetitionVO> getMyCompetitions(HttpServletRequest httpRequest) {
    User loginUser = userService.getLoginUser(httpRequest);
    long userId = loginUser.getId();

    // 获取用户所在团队
    List<Long> teamIds = teamMemberMapper.selectTeamIdsByUserId(userId);
    if (teamIds.isEmpty()) {
      return new ArrayList<>();
    }

    // 查询团队报名的比赛
    QueryWrapper<CompetitionRegistration> queryWrapper = new QueryWrapper<>();
    queryWrapper.in("team_id", teamIds);
    queryWrapper.eq("is_delete", 0);
    List<CompetitionRegistration> registrations = registrationMapper.selectList(queryWrapper);

    if (registrations.isEmpty()) {
      return new ArrayList<>();
    }

    Set<Long> competitionIds =
        registrations.stream()
            .map(CompetitionRegistration::getCompetitionId)
            .collect(Collectors.toSet());

    List<Competition> competitions = this.listByIds(competitionIds);
    return competitions.stream()
        .map(comp -> convertToCompetitionVO(comp, loginUser))
        .collect(Collectors.toList());
  }

  @Override
  public List<CompetitionVO> getOngoingCompetitions() {
    QueryWrapper<Competition> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("status", 2);
    queryWrapper.eq("is_public", 1);
    queryWrapper.orderByDesc("start_time");
    queryWrapper.last("LIMIT 10");

    List<Competition> competitions = this.list(queryWrapper);
    return competitions.stream()
        .map(comp -> convertToCompetitionVO(comp, null))
        .collect(Collectors.toList());
  }

  // ===================== 私有方法 =====================

  /** 获取团队成员记录 */
  private TeamMember getTeamMember(long teamId, long userId) {
    QueryWrapper<TeamMember> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("team_id", teamId);
    queryWrapper.eq("user_id", userId);
    return teamMemberMapper.selectOne(queryWrapper);
  }

  /** 生成淘汰赛对阵 */
  private void generateEliminationMatches(long competitionId, List<Long> teamIds) {
    int teamCount = teamIds.size();
    // 计算轮次（向上取整到2的幂次）
    int rounds = (int) Math.ceil(Math.log(teamCount) / Math.log(2));

    // 第一轮对阵
    Collections.shuffle(teamIds);
    int matchNumber = 1;
    for (int i = 0; i < teamIds.size(); i += 2) {
      CompetitionMatch match = new CompetitionMatch();
      match.setCompetitionId(competitionId);
      match.setRound(1);
      match.setMatchNumber(matchNumber++);
      match.setTeamAId(teamIds.get(i));
      match.setTeamBId(i + 1 < teamIds.size() ? teamIds.get(i + 1) : null);
      match.setStatus(0);
      matchMapper.insert(match);
    }
  }

  /** 生成循环赛对阵 */
  private void generateRoundRobinMatches(long competitionId, List<Long> teamIds) {
    int n = teamIds.size();
    int round = 1;
    int matchNumber = 1;

    // 使用循环算法生成对阵
    for (int i = 0; i < n - 1; i++) {
      for (int j = i + 1; j < n; j++) {
        CompetitionMatch match = new CompetitionMatch();
        match.setCompetitionId(competitionId);
        match.setRound(round++);
        match.setMatchNumber(matchNumber++);
        match.setTeamAId(teamIds.get(i));
        match.setTeamBId(teamIds.get(j));
        match.setStatus(0);
        matchMapper.insert(match);
      }
    }
  }

  /** 生成团队AC赛对阵 */
  private void generateTeamACMatches(long competitionId, List<Long> teamIds) {
    // 团队AC赛：所有团队同时参赛，按AC数排名
    Collections.shuffle(teamIds);
    for (int i = 0; i < teamIds.size(); i++) {
      CompetitionMatch match = new CompetitionMatch();
      match.setCompetitionId(competitionId);
      match.setRound(1);
      match.setMatchNumber(i + 1);
      match.setTeamAId(teamIds.get(i));
      match.setTeamBId(null); // AC赛没有对手
      match.setStatus(0);
      matchMapper.insert(match);
    }
  }

  /** 转换为 CompetitionVO */
  private CompetitionVO convertToCompetitionVO(Competition competition, User loginUser) {
    if (competition == null) {
      return null;
    }

    CompetitionVO vo = new CompetitionVO();
    BeanUtils.copyProperties(competition, vo);

    // 类型名称
    vo.setTypeName(getTypeName(competition.getType()));

    // 状态名称
    vo.setStatusName(getStatusName(competition.getStatus()));

    // 当前队伍数
    int currentTeams = baseMapper.countRegistrationsByCompetitionId(competition.getId());
    vo.setCurrentTeams(currentTeams);

    // 创建者信息
    User creator = userMapper.selectById(competition.getCreatorId());
    if (creator != null) {
      UserVO creatorVO = new UserVO();
      BeanUtils.copyProperties(creator, creatorVO);
      vo.setCreator(creatorVO);
    }

    // 当前用户是否已报名
    if (loginUser != null) {
      List<Long> userTeamIds = teamMemberMapper.selectTeamIdsByUserId(loginUser.getId());
      for (Long teamId : userTeamIds) {
        int exists = registrationMapper.countByCompetitionIdAndTeamId(competition.getId(), teamId);
        if (exists > 0) {
          vo.setHasRegistered(true);
          Team team = teamMapper.selectById(teamId);
          if (team != null) {
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            vo.setRegisteredTeam(teamVO);
          }
          break;
        }
      }
    }

    return vo;
  }

  /** 转换为 MatchVO */
  private MatchVO convertToMatchVO(CompetitionMatch match) {
    if (match == null) {
      return null;
    }

    MatchVO vo = new MatchVO();
    BeanUtils.copyProperties(match, vo);

    // 队伍A信息
    if (match.getTeamAId() != null) {
      Team teamA = teamMapper.selectById(match.getTeamAId());
      if (teamA != null) {
        TeamVO teamAVO = new TeamVO();
        BeanUtils.copyProperties(teamA, teamAVO);
        vo.setTeamA(teamAVO);
      }
    }

    // 队伍B信息
    if (match.getTeamBId() != null) {
      Team teamB = teamMapper.selectById(match.getTeamBId());
      if (teamB != null) {
        TeamVO teamBVO = new TeamVO();
        BeanUtils.copyProperties(teamB, teamBVO);
        vo.setTeamB(teamBVO);
      }
    }

    return vo;
  }

  /** 获取比赛类型名称 */
  private String getTypeName(String type) {
    Map<String, String> typeNames = new HashMap<>();
    typeNames.put("elimination", "淘汰赛");
    typeNames.put("round_robin", "循环赛");
    typeNames.put("team_ac", "团队AC赛");
    typeNames.put("battle", "对战");
    typeNames.put("relay", "接力赛");
    return typeNames.getOrDefault(type, type);
  }

  /** 获取比赛状态名称 */
  private String getStatusName(Integer status) {
    if (status == null) return "未知";
    switch (status) {
      case 0:
        return "未开始";
      case 1:
        return "报名中";
      case 2:
        return "进行中";
      case 3:
        return "已结束";
      default:
        return "未知";
    }
  }
}
