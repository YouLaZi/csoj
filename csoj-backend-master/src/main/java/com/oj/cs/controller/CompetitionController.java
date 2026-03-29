package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.dto.competition.*;
import com.oj.cs.model.vo.CompetitionVO;
import com.oj.cs.model.vo.MatchVO;
import com.oj.cs.model.vo.TeamVO;
import com.oj.cs.service.CompetitionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/** 比赛控制器 */
@Slf4j
@Tag(name = "比赛接口")
@RestController
@RequestMapping("/competition")
public class CompetitionController {

  @Resource private CompetitionService competitionService;

  @Operation(summary = "创建比赛")
  @PostMapping("/create")
  public BaseResponse<Long> createCompetition(
      @RequestBody CompetitionCreateRequest request, HttpServletRequest httpRequest) {
    long competitionId = competitionService.createCompetition(request, httpRequest);
    return ResultUtils.success(competitionId);
  }

  @Operation(summary = "更新比赛")
  @PutMapping("/update")
  public BaseResponse<Boolean> updateCompetition(
      @RequestBody CompetitionUpdateRequest request, HttpServletRequest httpRequest) {
    boolean result = competitionService.updateCompetition(request, httpRequest);
    return ResultUtils.success(result);
  }

  @Operation(summary = "删除比赛")
  @DeleteMapping("/delete/{id}")
  public BaseResponse<Boolean> deleteCompetition(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    boolean result = competitionService.deleteCompetition(competitionId, httpRequest);
    return ResultUtils.success(result);
  }

  @Operation(summary = "获取比赛详情")
  @GetMapping("/{id}")
  public BaseResponse<CompetitionVO> getCompetition(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    CompetitionVO competition = competitionService.getCompetitionById(competitionId, httpRequest);
    return ResultUtils.success(competition);
  }

  @Operation(summary = "分页查询比赛列表")
  @GetMapping("/list")
  public BaseResponse<Page<CompetitionVO>> listCompetitions(
      CompetitionQueryRequest request, HttpServletRequest httpRequest) {
    Page<CompetitionVO> page = competitionService.listCompetitions(request, httpRequest);
    return ResultUtils.success(page);
  }

  @Operation(summary = "报名比赛")
  @PostMapping("/register")
  public BaseResponse<Boolean> registerCompetition(
      @RequestBody CompetitionRegisterRequest request, HttpServletRequest httpRequest) {
    boolean result = competitionService.registerCompetition(request, httpRequest);
    return ResultUtils.success(result);
  }

  @Operation(summary = "取消报名")
  @PostMapping("/cancel/{id}")
  public BaseResponse<Boolean> cancelRegistration(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    boolean result = competitionService.cancelRegistration(competitionId, httpRequest);
    return ResultUtils.success(result);
  }

  @Operation(summary = "获取比赛报名列表")
  @GetMapping("/registrations/{id}")
  public BaseResponse<List<TeamVO>> getRegistrations(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    List<TeamVO> teams = competitionService.getRegistrations(competitionId, httpRequest);
    return ResultUtils.success(teams);
  }

  @Operation(summary = "开始比赛")
  @PostMapping("/start/{id}")
  public BaseResponse<Boolean> startCompetition(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    boolean result = competitionService.startCompetition(competitionId, httpRequest);
    return ResultUtils.success(result);
  }

  @Operation(summary = "结束比赛")
  @PostMapping("/end/{id}")
  public BaseResponse<Boolean> endCompetition(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    boolean result = competitionService.endCompetition(competitionId, httpRequest);
    return ResultUtils.success(result);
  }

  @Operation(summary = "生成对阵")
  @PostMapping("/generate-matches/{id}")
  public BaseResponse<Boolean> generateMatches(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    boolean result = competitionService.generateMatches(competitionId, httpRequest);
    return ResultUtils.success(result);
  }

  @Operation(summary = "获取对阵表")
  @GetMapping("/matches/{id}")
  public BaseResponse<List<MatchVO>> getMatchList(
      @PathVariable("id") long competitionId, HttpServletRequest httpRequest) {
    List<MatchVO> matches = competitionService.getMatchList(competitionId, httpRequest);
    return ResultUtils.success(matches);
  }

  @Operation(summary = "获取我报名的比赛")
  @GetMapping("/my")
  public BaseResponse<List<CompetitionVO>> getMyCompetitions(HttpServletRequest httpRequest) {
    List<CompetitionVO> competitions = competitionService.getMyCompetitions(httpRequest);
    return ResultUtils.success(competitions);
  }

  @Operation(summary = "获取进行中的比赛")
  @GetMapping("/ongoing")
  public BaseResponse<List<CompetitionVO>> getOngoingCompetitions() {
    List<CompetitionVO> competitions = competitionService.getOngoingCompetitions();
    return ResultUtils.success(competitions);
  }
}
