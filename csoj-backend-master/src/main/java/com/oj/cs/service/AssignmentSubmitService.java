package com.oj.cs.service;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.assignment.AssignmentSubmitRequest;
import com.oj.cs.model.dto.assignment.GradeRequest;
import com.oj.cs.model.entity.AssignmentSubmit;
import com.oj.cs.model.vo.AssignmentSubmitVO;

/** 作业提交服务 */
public interface AssignmentSubmitService extends IService<AssignmentSubmit> {

  /**
   * 提交作业
   *
   * @param submitRequest 提交请求
   * @param userId 学生ID
   * @return 提交ID
   */
  Long submitAssignment(AssignmentSubmitRequest submitRequest, Long userId);

  /**
   * 批改作业
   *
   * @param gradeRequest 批改请求
   * @param teacherId 教师ID
   * @return 是否成功
   */
  Boolean gradeAssignment(GradeRequest gradeRequest, Long teacherId);

  /**
   * 批量批改作业
   *
   * @param gradeRequests 批改请求列表
   * @param teacherId 教师ID
   * @return 成功数量
   */
  Integer batchGradeAssignments(List<GradeRequest> gradeRequests, Long teacherId);

  /**
   * 根据ID获取提交VO
   *
   * @param id 提交ID
   * @return 提交VO
   */
  AssignmentSubmitVO getSubmitVOById(Long id);

  /**
   * 根据作业ID和学生ID获取提交
   *
   * @param assignmentId 作业ID
   * @param userId 学生ID
   * @return 提交VO
   */
  AssignmentSubmitVO getSubmitByAssignmentAndUser(Long assignmentId, Long userId);

  /**
   * 分页查询作业提交
   *
   * @param current 当前页
   * @param size 每页大小
   * @param assignmentId 作业ID
   * @param status 状态（可选）
   * @param userId 学生ID（可选）
   * @return 分页结果
   */
  Page<AssignmentSubmitVO> listSubmitByPage(
      long current, long size, Long assignmentId, String status, Long userId);

  /**
   * 获取作业的所有提交
   *
   * @param assignmentId 作业ID
   * @return 提交VO列表
   */
  List<AssignmentSubmitVO> listSubmitsByAssignmentId(Long assignmentId);

  /**
   * 获取学生的所有提交
   *
   * @param userId 学生ID
   * @return 提交VO列表
   */
  List<AssignmentSubmitVO> listSubmitsByUserId(Long userId);

  /**
   * 实体转VO
   *
   * @param submit 提交实体
   * @return 提交VO
   */
  AssignmentSubmitVO getSubmitVO(AssignmentSubmit submit);

  /**
   * 实体列表转VO列表
   *
   * @param submitList 提交实体列表
   * @return 提交VO列表
   */
  List<AssignmentSubmitVO> getSubmitVO(List<AssignmentSubmit> submitList);

  /**
   * 构建查询条件
   *
   * @param assignmentId 作业ID
   * @param status 状态
   * @param userId 学生ID
   * @return QueryWrapper
   */
  QueryWrapper<AssignmentSubmit> getQueryWrapper(Long assignmentId, String status, Long userId);

  /**
   * 导出作业成绩到 Excel
   *
   * @param assignmentId 作业ID
   * @param response HTTP响应
   */
  void exportGradesToExcel(Long assignmentId, HttpServletResponse response);
}
