package com.oj.cs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.model.dto.assignment.AssignmentAddRequest;
import com.oj.cs.model.dto.assignment.AssignmentQueryRequest;
import com.oj.cs.model.dto.assignment.AssignmentUpdateRequest;
import com.oj.cs.model.entity.Assignment;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.AssignmentVO;
import com.oj.cs.service.AssignmentService;
import com.oj.cs.service.AssignmentSubmitService;
import com.oj.cs.service.UserService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/** 作业服务实现 */
@Service
@Slf4j
public class AssignmentServiceImpl
    extends ServiceImpl<com.oj.cs.mapper.AssignmentMapper, Assignment>
    implements AssignmentService {

  @Resource private UserService userService;

  @Resource @Lazy private AssignmentSubmitService assignmentSubmitService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long createAssignment(AssignmentAddRequest assignmentAddRequest, Long userId) {
    // 参数校验
    if (assignmentAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    if (StrUtil.isBlank(assignmentAddRequest.getTitle())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "作业标题不能为空");
    }
    if (assignmentAddRequest.getQuestionIds() == null
        || assignmentAddRequest.getQuestionIds().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "作业题目不能为空");
    }
    if (assignmentAddRequest.getTotalScore() == null || assignmentAddRequest.getTotalScore() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "总分必须大于0");
    }

    // 获取创建用户
    User user = userService.getById(userId);
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);

    // 创建作业
    Assignment assignment = new Assignment();
    assignment.setTitle(assignmentAddRequest.getTitle());
    assignment.setDescription(assignmentAddRequest.getDescription());
    assignment.setType(
        StrUtil.isNotBlank(assignmentAddRequest.getType())
            ? assignmentAddRequest.getType()
            : "PRACTICE");
    assignment.setQuestionIds(JSONUtil.toJsonStr(assignmentAddRequest.getQuestionIds()));
    assignment.setDifficulty(
        StrUtil.isNotBlank(assignmentAddRequest.getDifficulty())
            ? assignmentAddRequest.getDifficulty()
            : "MEDIUM");
    assignment.setTotalScore(assignmentAddRequest.getTotalScore());
    assignment.setPassScore(
        assignmentAddRequest.getPassScore() != null
            ? assignmentAddRequest.getPassScore()
            : assignmentAddRequest.getTotalScore() * 6 / 10);
    assignment.setDeadline(assignmentAddRequest.getDeadline());
    assignment.setUserId(userId);
    assignment.setClassId(assignmentAddRequest.getClassId());
    assignment.setIsPublic(
        assignmentAddRequest.getIsPublic() != null ? assignmentAddRequest.getIsPublic() : false);
    assignment.setStatus("DRAFT");
    assignment.setSubmitCount(0);

    boolean result = this.save(assignment);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    return assignment.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean updateAssignment(AssignmentUpdateRequest assignmentUpdateRequest, Long userId) {
    // 参数校验
    if (assignmentUpdateRequest == null || assignmentUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查作业是否存在
    Assignment oldAssignment = this.getById(assignmentUpdateRequest.getId());
    ThrowUtils.throwIf(oldAssignment == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限（只有创建者可以修改）
    if (!oldAssignment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 已发布的作业只能修改截止时间和状态
    if ("PUBLISHED".equals(oldAssignment.getStatus())) {
      Assignment update = new Assignment();
      update.setId(assignmentUpdateRequest.getId());
      update.setDeadline(assignmentUpdateRequest.getDeadline());
      if (StrUtil.isNotBlank(assignmentUpdateRequest.getStatus())) {
        update.setStatus(assignmentUpdateRequest.getStatus());
      }
      return this.updateById(update);
    }

    // 草稿状态的作业可以修改所有字段
    Assignment assignment = new Assignment();
    assignment.setId(assignmentUpdateRequest.getId());

    if (StrUtil.isNotBlank(assignmentUpdateRequest.getTitle())) {
      assignment.setTitle(assignmentUpdateRequest.getTitle());
    }
    if (assignmentUpdateRequest.getDescription() != null) {
      assignment.setDescription(assignmentUpdateRequest.getDescription());
    }
    if (StrUtil.isNotBlank(assignmentUpdateRequest.getType())) {
      assignment.setType(assignmentUpdateRequest.getType());
    }
    if (assignmentUpdateRequest.getQuestionIds() != null) {
      assignment.setQuestionIds(JSONUtil.toJsonStr(assignmentUpdateRequest.getQuestionIds()));
    }
    if (StrUtil.isNotBlank(assignmentUpdateRequest.getDifficulty())) {
      assignment.setDifficulty(assignmentUpdateRequest.getDifficulty());
    }
    if (assignmentUpdateRequest.getTotalScore() != null) {
      assignment.setTotalScore(assignmentUpdateRequest.getTotalScore());
    }
    if (assignmentUpdateRequest.getPassScore() != null) {
      assignment.setPassScore(assignmentUpdateRequest.getPassScore());
    }
    if (assignmentUpdateRequest.getDeadline() != null) {
      assignment.setDeadline(assignmentUpdateRequest.getDeadline());
    }
    if (assignmentUpdateRequest.getClassId() != null) {
      assignment.setClassId(assignmentUpdateRequest.getClassId());
    }
    if (assignmentUpdateRequest.getIsPublic() != null) {
      assignment.setIsPublic(assignmentUpdateRequest.getIsPublic());
    }
    if (StrUtil.isNotBlank(assignmentUpdateRequest.getStatus())) {
      assignment.setStatus(assignmentUpdateRequest.getStatus());
    }

    return this.updateById(assignment);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean deleteAssignment(Long id, Long userId) {
    // 参数校验
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查作业是否存在
    Assignment assignment = this.getById(id);
    ThrowUtils.throwIf(assignment == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限
    if (!assignment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 已发布的作业不能删除
    if ("PUBLISHED".equals(assignment.getStatus())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "已发布的作业不能删除，请先关闭");
    }

    return this.removeById(id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean publishAssignment(Long id, Long userId) {
    // 参数校验
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查作业是否存在
    Assignment assignment = this.getById(id);
    ThrowUtils.throwIf(assignment == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限
    if (!assignment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 只有草稿状态可以发布
    if (!"DRAFT".equals(assignment.getStatus())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有草稿状态的作业可以发布");
    }

    // 更新状态
    assignment.setStatus("PUBLISHED");
    return this.updateById(assignment);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean closeAssignment(Long id, Long userId) {
    // 参数校验
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查作业是否存在
    Assignment assignment = this.getById(id);
    ThrowUtils.throwIf(assignment == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限
    if (!assignment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 只有已发布状态可以关闭
    if (!"PUBLISHED".equals(assignment.getStatus())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有已发布的作业可以关闭");
    }

    // 更新状态
    assignment.setStatus("CLOSED");
    return this.updateById(assignment);
  }

  @Override
  public AssignmentVO getAssignmentVOById(Long id, Long userId) {
    // 参数校验
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 查询作业
    Assignment assignment = this.getById(id);
    ThrowUtils.throwIf(assignment == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限（已发布的作业所有人可见，草稿只有创建者可见）
    User user = userService.getById(userId);
    boolean isAdmin = userService.isAdmin(user);
    if (!"PUBLISHED".equals(assignment.getStatus())
        && !assignment.getUserId().equals(userId)
        && !isAdmin) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 转换为VO
    AssignmentVO assignmentVO = getAssignmentVO(assignment);

    // 查询当前用户是否已提交
    if (userId != null && "PUBLISHED".equals(assignment.getStatus())) {
      boolean hasSubmitted =
          assignmentSubmitService.getSubmitByAssignmentAndUser(id, userId) != null;
      assignmentVO.setHasSubmitted(hasSubmitted);
    }

    return assignmentVO;
  }

  @Override
  public Page<AssignmentVO> listAssignmentByPage(
      long current, long size, AssignmentQueryRequest queryRequest, Long userId, boolean isAdmin) {
    // 构建查询条件
    QueryWrapper<Assignment> queryWrapper = getQueryWrapper(queryRequest);

    // 非管理员只能看到已发布的作业或自己创建的作业
    if (!isAdmin) {
      queryWrapper.and(wrapper -> wrapper.eq("status", "PUBLISHED").or().eq("user_id", userId));
    }

    // 处理排序
    String sortField = queryRequest.getSortField();
    String sortOrder = queryRequest.getSortOrder();
    if (StrUtil.isNotBlank(sortField)) {
      if ("desc".equalsIgnoreCase(sortOrder)) {
        queryWrapper.orderByDesc(sortField);
      } else {
        queryWrapper.orderByAsc(sortField);
      }
    } else {
      queryWrapper.orderByDesc("create_time");
    }

    // 分页查询
    Page<Assignment> assignmentPage = this.page(new Page<>(current, size), queryWrapper);

    // 转换为VO
    Page<AssignmentVO> assignmentVOPage = new Page<>(current, size, assignmentPage.getTotal());
    List<AssignmentVO> assignmentVOList = getAssignmentVO(assignmentPage.getRecords());
    assignmentVOPage.setRecords(assignmentVOList);

    return assignmentVOPage;
  }

  @Override
  public AssignmentVO getAssignmentVO(Assignment assignment) {
    if (assignment == null) {
      return null;
    }

    AssignmentVO assignmentVO = new AssignmentVO();
    assignmentVO.setId(assignment.getId());
    assignmentVO.setTitle(assignment.getTitle());
    assignmentVO.setDescription(assignment.getDescription());
    assignmentVO.setType(assignment.getType());
    assignmentVO.setDifficulty(assignment.getDifficulty());
    assignmentVO.setTotalScore(assignment.getTotalScore());
    assignmentVO.setPassScore(assignment.getPassScore());
    assignmentVO.setDeadline(assignment.getDeadline());
    assignmentVO.setUserId(assignment.getUserId());
    assignmentVO.setClassId(assignment.getClassId());
    assignmentVO.setIsPublic(assignment.getIsPublic());
    assignmentVO.setStatus(assignment.getStatus());
    assignmentVO.setSubmitCount(assignment.getSubmitCount());
    assignmentVO.setCreateTime(assignment.getCreateTime());
    assignmentVO.setUpdateTime(assignment.getUpdateTime());

    // 解析题目ID列表
    if (StrUtil.isNotBlank(assignment.getQuestionIds())) {
      try {
        assignmentVO.setQuestionIds(JSONUtil.toList(assignment.getQuestionIds(), Long.class));
      } catch (Exception e) {
        log.error("解析题目ID列表失败", e);
      }
    }

    // 获取创建用户信息
    User user = userService.getById(assignment.getUserId());
    if (user != null) {
      assignmentVO.setUserName(user.getUserName());
    }

    return assignmentVO;
  }

  @Override
  public List<AssignmentVO> getAssignmentVO(List<Assignment> assignmentList) {
    if (assignmentList == null || assignmentList.isEmpty()) {
      return new ArrayList<>();
    }

    return assignmentList.stream().map(this::getAssignmentVO).collect(Collectors.toList());
  }

  @Override
  public QueryWrapper<Assignment> getQueryWrapper(AssignmentQueryRequest queryRequest) {
    QueryWrapper<Assignment> queryWrapper = new QueryWrapper<>();

    if (queryRequest == null) {
      return queryWrapper;
    }

    // 标题模糊查询
    String title = queryRequest.getTitle();
    if (StrUtil.isNotBlank(title)) {
      queryWrapper.like("title", title);
    }

    // 作业类型精确查询
    String type = queryRequest.getType();
    if (StrUtil.isNotBlank(type)) {
      queryWrapper.eq("type", type);
    }

    // 难度等级精确查询
    String difficulty = queryRequest.getDifficulty();
    if (StrUtil.isNotBlank(difficulty)) {
      queryWrapper.eq("difficulty", difficulty);
    }

    // 状态精确查询
    String status = queryRequest.getStatus();
    if (StrUtil.isNotBlank(status)) {
      queryWrapper.eq("status", status);
    }

    // 班级ID精确查询
    String classId = queryRequest.getClassId();
    if (StrUtil.isNotBlank(classId)) {
      queryWrapper.eq("class_id", classId);
    }

    // 创建用户ID精确查询
    Long userId = queryRequest.getUserId();
    if (userId != null) {
      queryWrapper.eq("user_id", userId);
    }

    // 截止时间范围查询
    Date deadlineStart = queryRequest.getDeadlineStart();
    Date deadlineEnd = queryRequest.getDeadlineEnd();
    if (deadlineStart != null) {
      queryWrapper.ge("deadline", deadlineStart);
    }
    if (deadlineEnd != null) {
      queryWrapper.le("deadline", deadlineEnd);
    }

    return queryWrapper;
  }
}
