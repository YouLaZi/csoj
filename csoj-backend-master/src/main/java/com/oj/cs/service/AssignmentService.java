package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.assignment.AssignmentAddRequest;
import com.oj.cs.model.dto.assignment.AssignmentQueryRequest;
import com.oj.cs.model.dto.assignment.AssignmentUpdateRequest;
import com.oj.cs.model.entity.Assignment;
import com.oj.cs.model.vo.AssignmentVO;

/** 作业服务 */
public interface AssignmentService extends IService<Assignment> {

  /**
   * 创建作业
   *
   * @param assignmentAddRequest 创建请求
   * @param userId 创建用户ID
   * @return 作业ID
   */
  Long createAssignment(AssignmentAddRequest assignmentAddRequest, Long userId);

  /**
   * 更新作业
   *
   * @param assignmentUpdateRequest 更新请求
   * @param userId 操作用户ID
   * @return 是否成功
   */
  Boolean updateAssignment(AssignmentUpdateRequest assignmentUpdateRequest, Long userId);

  /**
   * 删除作业
   *
   * @param id 作业ID
   * @param userId 操作用户ID
   * @return 是否成功
   */
  Boolean deleteAssignment(Long id, Long userId);

  /**
   * 发布作业
   *
   * @param id 作业ID
   * @param userId 操作用户ID
   * @return 是否成功
   */
  Boolean publishAssignment(Long id, Long userId);

  /**
   * 关闭作业
   *
   * @param id 作业ID
   * @param userId 操作用户ID
   * @return 是否成功
   */
  Boolean closeAssignment(Long id, Long userId);

  /**
   * 根据ID获取作业VO
   *
   * @param id 作业ID
   * @param userId 查询用户ID
   * @return 作业VO
   */
  AssignmentVO getAssignmentVOById(Long id, Long userId);

  /**
   * 分页查询作业
   *
   * @param current 当前页
   * @param size 每页大小
   * @param queryRequest 查询条件
   * @param userId 查询用户ID
   * @param isAdmin 是否管理员
   * @return 分页结果
   */
  Page<AssignmentVO> listAssignmentByPage(
      long current, long size, AssignmentQueryRequest queryRequest, Long userId, boolean isAdmin);

  /**
   * 实体转VO
   *
   * @param assignment 作业实体
   * @return 作业VO
   */
  AssignmentVO getAssignmentVO(Assignment assignment);

  /**
   * 实体列表转VO列表
   *
   * @param assignmentList 作业实体列表
   * @return 作业VO列表
   */
  List<AssignmentVO> getAssignmentVO(List<Assignment> assignmentList);

  /**
   * 构建查询条件
   *
   * @param queryRequest 查询条件
   * @return QueryWrapper
   */
  QueryWrapper<Assignment> getQueryWrapper(AssignmentQueryRequest queryRequest);
}
