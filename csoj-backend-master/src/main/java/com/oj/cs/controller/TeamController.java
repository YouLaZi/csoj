package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.team.*;
import com.oj.cs.model.vo.TeamVO;
import com.oj.cs.service.TeamService;

import lombok.extern.slf4j.Slf4j;

/** 团队控制器 */
@Slf4j
@RestController
@RequestMapping("/team")
public class TeamController {

  @Resource private TeamService teamService;

  /** 创建团队 */
  @PostMapping("/create")
  public BaseResponse<Long> createTeam(
      @RequestBody TeamCreateRequest request, HttpServletRequest httpRequest) {
    if (request == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long teamId = teamService.createTeam(request, httpRequest);
    return ResultUtils.success(teamId);
  }

  /** 更新团队 */
  @PutMapping("/update")
  public BaseResponse<Boolean> updateTeam(
      @RequestBody TeamUpdateRequest request, HttpServletRequest httpRequest) {
    if (request == null || request.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = teamService.updateTeam(request, httpRequest);
    return ResultUtils.success(result);
  }

  /** 解散团队 */
  @DeleteMapping("/{teamId}")
  public BaseResponse<Boolean> deleteTeam(
      @PathVariable Long teamId, HttpServletRequest httpRequest) {
    if (teamId == null || teamId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = teamService.deleteTeam(teamId, httpRequest);
    return ResultUtils.success(result);
  }

  /** 获取团队详情 */
  @GetMapping("/{teamId}")
  public BaseResponse<TeamVO> getTeam(@PathVariable Long teamId, HttpServletRequest httpRequest) {
    if (teamId == null || teamId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    TeamVO teamVO = teamService.getTeamById(teamId, httpRequest);
    return ResultUtils.success(teamVO);
  }

  /** 分页查询团队列表 */
  @GetMapping("/list")
  public BaseResponse<Page<TeamVO>> listTeams(
      TeamQueryRequest request, HttpServletRequest httpRequest) {
    Page<TeamVO> teamVOPage = teamService.listTeams(request, httpRequest);
    return ResultUtils.success(teamVOPage);
  }

  /** 加入团队 */
  @PostMapping("/join")
  public BaseResponse<Boolean> joinTeam(
      @RequestBody JoinTeamRequest request, HttpServletRequest httpRequest) {
    if (request == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = teamService.joinTeam(request, httpRequest);
    return ResultUtils.success(result);
  }

  /** 退出团队 */
  @PostMapping("/{teamId}/quit")
  public BaseResponse<Boolean> quitTeam(@PathVariable Long teamId, HttpServletRequest httpRequest) {
    if (teamId == null || teamId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = teamService.quitTeam(teamId, httpRequest);
    return ResultUtils.success(result);
  }

  /** 移除成员 */
  @PostMapping("/{teamId}/kick")
  public BaseResponse<Boolean> kickMember(
      @PathVariable Long teamId, @RequestParam Long userId, HttpServletRequest httpRequest) {
    if (teamId == null || teamId <= 0 || userId == null || userId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = teamService.kickMember(teamId, userId, httpRequest);
    return ResultUtils.success(result);
  }

  /** 转让队长 */
  @PostMapping("/{teamId}/transfer")
  public BaseResponse<Boolean> transferLeader(
      @PathVariable Long teamId, @RequestParam Long newLeaderId, HttpServletRequest httpRequest) {
    if (teamId == null || teamId <= 0 || newLeaderId == null || newLeaderId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = teamService.transferLeader(teamId, newLeaderId, httpRequest);
    return ResultUtils.success(result);
  }

  /** 生成邀请码 */
  @PostMapping("/{teamId}/invite")
  public BaseResponse<String> generateInviteCode(
      @PathVariable Long teamId, HttpServletRequest httpRequest) {
    if (teamId == null || teamId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    String inviteCode = teamService.generateInviteCode(teamId, httpRequest);
    return ResultUtils.success(inviteCode);
  }

  /** 获取我的团队 */
  @GetMapping("/my")
  public BaseResponse<List<TeamVO>> getMyTeams(HttpServletRequest httpRequest) {
    List<TeamVO> myTeams = teamService.getMyTeams(httpRequest);
    return ResultUtils.success(myTeams);
  }

  /** 团队排行榜 */
  @GetMapping("/ranking")
  public BaseResponse<Page<TeamVO>> getTeamRanking(TeamQueryRequest request) {
    Page<TeamVO> teamVOPage = teamService.getTeamRanking(request);
    return ResultUtils.success(teamVOPage);
  }
}
