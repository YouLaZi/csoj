package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
import com.oj.cs.model.dto.assignment.AssignmentAddRequest;
import com.oj.cs.model.dto.assignment.AssignmentQueryRequest;
import com.oj.cs.model.dto.assignment.AssignmentSubmitRequest;
import com.oj.cs.model.dto.assignment.AssignmentUpdateRequest;
import com.oj.cs.model.dto.assignment.GradeRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.AssignmentSubmitVO;
import com.oj.cs.model.vo.AssignmentVO;
import com.oj.cs.service.AssignmentService;
import com.oj.cs.service.AssignmentSubmitService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 作业接口 */
@RestController
@RequestMapping("/assignment")
@Slf4j
public class AssignmentController {

  @Resource private AssignmentService assignmentService;

  @Resource private AssignmentSubmitService assignmentSubmitService;

  @Resource private UserService userService;

  // region 作业管理

  /**
   * 创建作业
   *
   * @param assignmentAddRequest 创建请求
   * @param request 请求
   * @return 作业ID
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "CREATE", description = "创建作业")
  public BaseResponse<Long> addAssignment(
      @RequestBody AssignmentAddRequest assignmentAddRequest, HttpServletRequest request) {
    if (assignmentAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Long result = assignmentService.createAssignment(assignmentAddRequest, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 更新作业
   *
   * @param assignmentUpdateRequest 更新请求
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "UPDATE", description = "更新作业")
  public BaseResponse<Boolean> updateAssignment(
      @RequestBody AssignmentUpdateRequest assignmentUpdateRequest, HttpServletRequest request) {
    if (assignmentUpdateRequest == null || assignmentUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = assignmentService.updateAssignment(assignmentUpdateRequest, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 删除作业
   *
   * @param deleteRequest 删除请求
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "DELETE", description = "删除作业")
  public BaseResponse<Boolean> deleteAssignment(
      @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
    if (deleteRequest == null || deleteRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = assignmentService.deleteAssignment(deleteRequest.getId(), loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 发布作业
   *
   * @param id 作业ID
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/publish")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "UPDATE", description = "发布作业")
  public BaseResponse<Boolean> publishAssignment(
      @RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = assignmentService.publishAssignment(id, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 关闭作业
   *
   * @param id 作业ID
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/close")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "UPDATE", description = "关闭作业")
  public BaseResponse<Boolean> closeAssignment(@RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = assignmentService.closeAssignment(id, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 根据ID获取作业
   *
   * @param id 作业ID
   * @param request 请求
   * @return 作业VO
   */
  @GetMapping("/get")
  public BaseResponse<AssignmentVO> getAssignmentById(
      @RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    AssignmentVO result = assignmentService.getAssignmentVOById(id, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 分页获取作业列表
   *
   * @param assignmentQueryRequest 查询条件
   * @param request 请求
   * @return 分页结果
   */
  @PostMapping("/list/page")
  public BaseResponse<Page<AssignmentVO>> listAssignmentByPage(
      @RequestBody AssignmentQueryRequest assignmentQueryRequest, HttpServletRequest request) {
    if (assignmentQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long current = assignmentQueryRequest.getCurrent();
    long size = assignmentQueryRequest.getPageSize();

    User loginUser = userService.getLoginUser(request);
    boolean isAdmin = userService.isAdmin(loginUser);

    Page<AssignmentVO> result =
        assignmentService.listAssignmentByPage(
            current, size, assignmentQueryRequest, loginUser.getId(), isAdmin);
    return ResultUtils.success(result);
  }

  /**
   * 获取我创建的作业列表
   *
   * @param assignmentQueryRequest 查询条件
   * @param request 请求
   * @return 分页结果
   */
  @PostMapping("/list/my")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<AssignmentVO>> listMyAssignmentByPage(
      @RequestBody AssignmentQueryRequest assignmentQueryRequest, HttpServletRequest request) {
    if (assignmentQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long current = assignmentQueryRequest.getCurrent();
    long size = assignmentQueryRequest.getPageSize();

    User loginUser = userService.getLoginUser(request);
    assignmentQueryRequest.setUserId(loginUser.getId());

    Page<AssignmentVO> result =
        assignmentService.listAssignmentByPage(
            current, size, assignmentQueryRequest, loginUser.getId(), true);
    return ResultUtils.success(result);
  }

  // endregion

  // region 作业提交

  /**
   * 学生提交作业
   *
   * @param submitRequest 提交请求
   * @param request 请求
   * @return 提交ID
   */
  @PostMapping("/submit")
  @AuditLog(module = "作业管理", operationType = "CREATE", description = "提交作业")
  public BaseResponse<Long> submitAssignment(
      @RequestBody AssignmentSubmitRequest submitRequest, HttpServletRequest request) {
    if (submitRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Long result = assignmentSubmitService.submitAssignment(submitRequest, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 教师批改作业
   *
   * @param gradeRequest 批改请求
   * @param request 请求
   * @return 是否成功
   */
  @PostMapping("/grade")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "UPDATE", description = "批改作业")
  public BaseResponse<Boolean> gradeAssignment(
      @RequestBody GradeRequest gradeRequest, HttpServletRequest request) {
    if (gradeRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Boolean result = assignmentSubmitService.gradeAssignment(gradeRequest, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 批量批改作业
   *
   * @param gradeRequests 批改请求列表
   * @param request 请求
   * @return 成功数量
   */
  @PostMapping("/grade/batch")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "UPDATE", description = "批量批改作业")
  public BaseResponse<Integer> batchGradeAssignments(
      @RequestBody List<GradeRequest> gradeRequests, HttpServletRequest request) {
    if (gradeRequests == null || gradeRequests.isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    Integer result =
        assignmentSubmitService.batchGradeAssignments(gradeRequests, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 获取作业提交详情
   *
   * @param id 提交ID
   * @param request 请求
   * @return 提交VO
   */
  @GetMapping("/submit/get")
  public BaseResponse<AssignmentSubmitVO> getSubmitById(
      @RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    AssignmentSubmitVO result = assignmentSubmitService.getSubmitVOById(id);
    return ResultUtils.success(result);
  }

  /**
   * 获取作业的所有提交（教师）
   *
   * @param assignmentId 作业ID
   * @param request 请求
   * @return 提交列表
   */
  @GetMapping("/submit/list")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<List<AssignmentSubmitVO>> listSubmitsByAssignmentId(
      @RequestParam Long assignmentId, HttpServletRequest request) {
    if (assignmentId == null || assignmentId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    List<AssignmentSubmitVO> result =
        assignmentSubmitService.listSubmitsByAssignmentId(assignmentId);
    return ResultUtils.success(result);
  }

  /**
   * 分页获取作业提交
   *
   * @param assignmentId 作业ID
   * @param current 当前页
   * @param size 每页大小
   * @param status 状态（可选）
   * @param request 请求
   * @return 分页结果
   */
  @GetMapping("/submit/list/page")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<AssignmentSubmitVO>> listSubmitByPage(
      @RequestParam Long assignmentId,
      @RequestParam(defaultValue = "1") long current,
      @RequestParam(defaultValue = "10") long size,
      @RequestParam(required = false) String status,
      HttpServletRequest request) {
    if (assignmentId == null || assignmentId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Page<AssignmentSubmitVO> result =
        assignmentSubmitService.listSubmitByPage(current, size, assignmentId, status, null);
    return ResultUtils.success(result);
  }

  /**
   * 获取我的作业提交
   *
   * @param assignmentId 作业ID
   * @param request 请求
   * @return 提交VO
   */
  @GetMapping("/submit/my")
  public BaseResponse<AssignmentSubmitVO> getMySubmit(
      @RequestParam Long assignmentId, HttpServletRequest request) {
    if (assignmentId == null || assignmentId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    AssignmentSubmitVO result =
        assignmentSubmitService.getSubmitByAssignmentAndUser(assignmentId, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 获取我的所有提交
   *
   * @param request 请求
   * @return 提交列表
   */
  @GetMapping("/submit/list/my")
  public BaseResponse<List<AssignmentSubmitVO>> listMySubmits(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    List<AssignmentSubmitVO> result =
        assignmentSubmitService.listSubmitsByUserId(loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 导出作业成绩到 Excel
   *
   * @param assignmentId 作业ID
   * @param response HTTP响应
   * @param request 请求
   */
  @GetMapping("/export")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "作业管理", operationType = "EXPORT", description = "导出作业成绩")
  public void exportGrades(
      @RequestParam Long assignmentId, HttpServletResponse response, HttpServletRequest request) {
    if (assignmentId == null || assignmentId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    assignmentSubmitService.exportGradesToExcel(assignmentId, response);
  }

  // endregion
}
