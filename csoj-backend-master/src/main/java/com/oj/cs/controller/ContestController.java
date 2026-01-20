package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.annotation.AuditLog;
import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.DeleteRequest;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.contest.ContestAddRequest;
import com.oj.cs.model.dto.contest.ContestJoinRequest;
import com.oj.cs.model.dto.contest.ContestQueryRequest;
import com.oj.cs.model.dto.contest.ContestUpdateRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.ContestRankingVO;
import com.oj.cs.model.vo.ContestVO;
import com.oj.cs.service.ContestService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 比赛接口 */
@RestController
@RequestMapping("/contest")
@Slf4j
public class ContestController {

  @Resource private ContestService contestService;

  @Resource private UserService userService;

  // region 比赛管理

  /**
   * 创建比赛
   *
   * @param contestAddRequest 创建请求
   * @param request 请求
   * @return 比赛ID
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "比赛管理", operationType = "CREATE", description = "创建比赛")
  public BaseResponse<Long> addContest(
      @RequestBody ContestAddRequest contestAddRequest, HttpServletRequest request) {
    if (contestAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Long result = contestService.createContest(contestAddRequest, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 更新比赛
   *
   * @param contestUpdateRequest 更新请求
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "比赛管理", operationType = "UPDATE", description = "更新比赛")
  public BaseResponse<Boolean> updateContest(
      @RequestBody ContestUpdateRequest contestUpdateRequest, HttpServletRequest request) {
    if (contestUpdateRequest == null || contestUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = contestService.updateContest(contestUpdateRequest, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 删除比赛
   *
   * @param deleteRequest 删除请求
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "比赛管理", operationType = "DELETE", description = "删除比赛")
  public BaseResponse<Boolean> deleteContest(
      @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
    if (deleteRequest == null || deleteRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = contestService.deleteContest(deleteRequest.getId(), loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 根据ID获取比赛
   *
   * @param id 比赛ID
   * @param request 请求
   * @return 比赛VO
   */
  @GetMapping("/get")
  public BaseResponse<ContestVO> getContestById(@RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    ContestVO result = contestService.getContestVOById(id, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 分页获取比赛列表
   *
   * @param contestQueryRequest 查询条件
   * @param request 请求
   * @return 分页结果
   */
  @PostMapping("/list/page")
  public BaseResponse<Page<ContestVO>> listContestByPage(
      @RequestBody ContestQueryRequest contestQueryRequest, HttpServletRequest request) {
    if (contestQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long current = contestQueryRequest.getCurrent();
    long size = contestQueryRequest.getPageSize();

    User loginUser = userService.getLoginUser(request);
    boolean isAdmin = userService.isAdmin(loginUser);

    Page<ContestVO> result =
        contestService.listContestByPage(
            current, size, contestQueryRequest, loginUser.getId(), isAdmin);
    return ResultUtils.success(result);
  }

  /**
   * 获取我创建的比赛列表
   *
   * @param contestQueryRequest 查询条件
   * @param request 请求
   * @return 分页结果
   */
  @PostMapping("/list/my")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<ContestVO>> listMyContestByPage(
      @RequestBody ContestQueryRequest contestQueryRequest, HttpServletRequest request) {
    if (contestQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long current = contestQueryRequest.getCurrent();
    long size = contestQueryRequest.getPageSize();

    User loginUser = userService.getLoginUser(request);
    contestQueryRequest.setUserId(loginUser.getId());

    Page<ContestVO> result =
        contestService.listContestByPage(
            current, size, contestQueryRequest, loginUser.getId(), true);
    return ResultUtils.success(result);
  }

  // endregion

  // region 比赛参与

  /**
   * 参加比赛
   *
   * @param joinRequest 参加请求
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/join")
  @AuditLog(module = "比赛管理", operationType = "CREATE", description = "参加比赛")
  public BaseResponse<Boolean> joinContest(
      @RequestBody ContestJoinRequest joinRequest, HttpServletRequest request) {
    if (joinRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = contestService.joinContest(joinRequest, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 获取比赛排行榜
   *
   * @param contestId 比赛ID
   * @param request 请求
   * @return 排行榜列表
   */
  @GetMapping("/ranking")
  public BaseResponse<List<ContestRankingVO>> getContestRanking(
      @RequestParam Long contestId, HttpServletRequest request) {
    if (contestId == null || contestId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    List<ContestRankingVO> result = contestService.getContestRanking(contestId, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 获取比赛统计数据
   *
   * @param contestId 比赛ID
   * @param request 请求
   * @return 统计数据
   */
  @GetMapping("/statistics")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<com.oj.cs.model.vo.ContestStatisticsVO> getContestStatistics(
      @RequestParam Long contestId, HttpServletRequest request) {
    if (contestId == null || contestId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    com.oj.cs.model.vo.ContestStatisticsVO result = contestService.getContestStatistics(contestId);
    return ResultUtils.success(result);
  }

  // endregion
}
