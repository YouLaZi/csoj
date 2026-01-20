package com.oj.cs.service.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.model.dto.assignment.AssignmentSubmitRequest;
import com.oj.cs.model.dto.assignment.GradeExportDTO;
import com.oj.cs.model.dto.assignment.GradeRequest;
import com.oj.cs.model.entity.Assignment;
import com.oj.cs.model.entity.AssignmentSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.AssignmentSubmitVO;
import com.oj.cs.service.AssignmentService;
import com.oj.cs.service.AssignmentSubmitService;
import com.oj.cs.service.UserService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/** 作业提交服务实现 */
@Service
@Slf4j
public class AssignmentSubmitServiceImpl
    extends ServiceImpl<com.oj.cs.mapper.AssignmentSubmitMapper, AssignmentSubmit>
    implements AssignmentSubmitService {

  @Resource private AssignmentService assignmentService;

  @Resource private UserService userService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long submitAssignment(AssignmentSubmitRequest submitRequest, Long userId) {
    // 参数校验
    if (submitRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    if (submitRequest.getAssignmentId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "作业ID不能为空");
    }
    if (submitRequest.getQuestionIds() == null || submitRequest.getQuestionIds().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交题目不能为空");
    }

    // 检查作业是否存在且已发布
    Assignment assignment = assignmentService.getById(submitRequest.getAssignmentId());
    ThrowUtils.throwIf(assignment == null, ErrorCode.NOT_FOUND_ERROR, "作业不存在");
    ThrowUtils.throwIf(
        !"PUBLISHED".equals(assignment.getStatus()), ErrorCode.OPERATION_ERROR, "作业未发布");

    // 检查是否已截止
    if (assignment.getDeadline() != null && new Date().after(assignment.getDeadline())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "作业已截止");
    }

    // 检查是否已提交
    AssignmentSubmit existingSubmit =
        this.getOne(
            new QueryWrapper<AssignmentSubmit>()
                .eq("assignment_id", submitRequest.getAssignmentId())
                .eq("user_id", userId));
    if (existingSubmit != null) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "已提交过此作业");
    }

    // 创建提交记录
    AssignmentSubmit submit = new AssignmentSubmit();
    submit.setAssignmentId(submitRequest.getAssignmentId());
    submit.setUserId(userId);
    submit.setQuestionIds(JSONUtil.toJsonStr(submitRequest.getQuestionIds()));
    submit.setScores(null);
    submit.setTotalScore(0);
    submit.setStatus("SUBMITTED");
    submit.setSubmitTime(new Date());

    boolean result = this.save(submit);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    // 更新作业提交数量
    assignment.setSubmitCount(assignment.getSubmitCount() + 1);
    assignmentService.updateById(assignment);

    return submit.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean gradeAssignment(GradeRequest gradeRequest, Long teacherId) {
    // 参数校验
    if (gradeRequest == null || gradeRequest.getSubmitId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查提交是否存在
    AssignmentSubmit submit = this.getById(gradeRequest.getSubmitId());
    ThrowUtils.throwIf(submit == null, ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");

    // 检查作业权限
    Assignment assignment = assignmentService.getById(submit.getAssignmentId());
    ThrowUtils.throwIf(assignment == null, ErrorCode.NOT_FOUND_ERROR);
    ThrowUtils.throwIf(!assignment.getUserId().equals(teacherId), ErrorCode.NO_AUTH_ERROR);

    // 更新提交记录
    submit.setScores(JSONUtil.toJsonStr(gradeRequest.getScores()));
    submit.setTotalScore(gradeRequest.getTotalScore() != null ? gradeRequest.getTotalScore() : 0);
    submit.setStatus("GRADED");
    submit.setComment(gradeRequest.getComment());
    submit.setGradedBy(teacherId);
    submit.setGradedTime(new Date());

    return this.updateById(submit);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer batchGradeAssignments(List<GradeRequest> gradeRequests, Long teacherId) {
    if (gradeRequests == null || gradeRequests.isEmpty()) {
      return 0;
    }

    int successCount = 0;
    for (GradeRequest gradeRequest : gradeRequests) {
      try {
        Boolean result = gradeAssignment(gradeRequest, teacherId);
        if (result) {
          successCount++;
        }
      } catch (Exception e) {
        log.error("批改提交失败: submitId={}", gradeRequest.getSubmitId(), e);
      }
    }

    return successCount;
  }

  @Override
  public AssignmentSubmitVO getSubmitVOById(Long id) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    AssignmentSubmit submit = this.getById(id);
    ThrowUtils.throwIf(submit == null, ErrorCode.NOT_FOUND_ERROR);

    return getSubmitVO(submit);
  }

  @Override
  public AssignmentSubmitVO getSubmitByAssignmentAndUser(Long assignmentId, Long userId) {
    if (assignmentId == null || userId == null) {
      return null;
    }

    AssignmentSubmit submit =
        this.getOne(
            new QueryWrapper<AssignmentSubmit>()
                .eq("assignment_id", assignmentId)
                .eq("user_id", userId));

    return getSubmitVO(submit);
  }

  @Override
  public Page<AssignmentSubmitVO> listSubmitByPage(
      long current, long size, Long assignmentId, String status, Long userId) {
    QueryWrapper<AssignmentSubmit> queryWrapper = getQueryWrapper(assignmentId, status, userId);

    // 默认按提交时间倒序
    queryWrapper.orderByDesc("submit_time");

    Page<AssignmentSubmit> submitPage = this.page(new Page<>(current, size), queryWrapper);

    // 转换为VO
    Page<AssignmentSubmitVO> submitVOPage = new Page<>(current, size, submitPage.getTotal());
    List<AssignmentSubmitVO> submitVOList = getSubmitVO(submitPage.getRecords());
    submitVOPage.setRecords(submitVOList);

    return submitVOPage;
  }

  @Override
  public List<AssignmentSubmitVO> listSubmitsByAssignmentId(Long assignmentId) {
    if (assignmentId == null) {
      return new ArrayList<>();
    }

    QueryWrapper<AssignmentSubmit> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("assignment_id", assignmentId);
    queryWrapper.orderByDesc("submit_time");

    List<AssignmentSubmit> submitList = this.list(queryWrapper);
    return getSubmitVO(submitList);
  }

  @Override
  public List<AssignmentSubmitVO> listSubmitsByUserId(Long userId) {
    if (userId == null) {
      return new ArrayList<>();
    }

    QueryWrapper<AssignmentSubmit> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("user_id", userId);
    queryWrapper.orderByDesc("submit_time");

    List<AssignmentSubmit> submitList = this.list(queryWrapper);
    return getSubmitVO(submitList);
  }

  @Override
  public AssignmentSubmitVO getSubmitVO(AssignmentSubmit submit) {
    if (submit == null) {
      return null;
    }

    AssignmentSubmitVO submitVO = new AssignmentSubmitVO();
    submitVO.setId(submit.getId());
    submitVO.setAssignmentId(submit.getAssignmentId());
    submitVO.setUserId(submit.getUserId());
    submitVO.setTotalScore(submit.getTotalScore());
    submitVO.setStatus(submit.getStatus());
    submitVO.setComment(submit.getComment());
    submitVO.setGradedBy(submit.getGradedBy());
    submitVO.setGradedTime(submit.getGradedTime());
    submitVO.setSubmitTime(submit.getSubmitTime());
    submitVO.setCreateTime(submit.getCreateTime());
    submitVO.setUpdateTime(submit.getUpdateTime());

    // 解析题目ID列表
    if (StrUtil.isNotBlank(submit.getQuestionIds())) {
      try {
        submitVO.setQuestionIds(JSONUtil.toList(submit.getQuestionIds(), Long.class));
      } catch (Exception e) {
        log.error("解析题目ID列表失败", e);
      }
    }

    // 解析得分
    if (StrUtil.isNotBlank(submit.getScores())) {
      try {
        submitVO.setScores(JSONUtil.toBean(submit.getScores(), Map.class));
      } catch (Exception e) {
        log.error("解析得分失败", e);
      }
    }

    // 获取作业信息
    Assignment assignment = assignmentService.getById(submit.getAssignmentId());
    if (assignment != null) {
      submitVO.setAssignmentTitle(assignment.getTitle());
    }

    // 获取学生信息
    User student = userService.getById(submit.getUserId());
    if (student != null) {
      submitVO.setUserName(student.getUserName());
      submitVO.setUserAccount(student.getUserAccount());
    }

    // 获取批改教师信息
    if (submit.getGradedBy() != null) {
      User teacher = userService.getById(submit.getGradedBy());
      if (teacher != null) {
        submitVO.setGradedByName(teacher.getUserName());
      }
    }

    return submitVO;
  }

  @Override
  public List<AssignmentSubmitVO> getSubmitVO(List<AssignmentSubmit> submitList) {
    if (submitList == null || submitList.isEmpty()) {
      return new ArrayList<>();
    }

    return submitList.stream().map(this::getSubmitVO).collect(Collectors.toList());
  }

  @Override
  public QueryWrapper<AssignmentSubmit> getQueryWrapper(
      Long assignmentId, String status, Long userId) {
    QueryWrapper<AssignmentSubmit> queryWrapper = new QueryWrapper<>();

    if (assignmentId != null) {
      queryWrapper.eq("assignment_id", assignmentId);
    }

    if (StrUtil.isNotBlank(status)) {
      queryWrapper.eq("status", status);
    }

    if (userId != null) {
      queryWrapper.eq("user_id", userId);
    }

    return queryWrapper;
  }

  @Override
  public void exportGradesToExcel(Long assignmentId, HttpServletResponse response) {
    // 参数校验
    if (assignmentId == null || assignmentId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 获取作业信息
    Assignment assignment = assignmentService.getById(assignmentId);
    ThrowUtils.throwIf(assignment == null, ErrorCode.NOT_FOUND_ERROR, "作业不存在");

    // 查询所有已批改的提交
    QueryWrapper<AssignmentSubmit> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("assignment_id", assignmentId);
    queryWrapper.eq("status", "GRADED");
    queryWrapper.orderByDesc("total_score");

    List<AssignmentSubmit> submitList = this.list(queryWrapper);

    // 转换为导出DTO
    List<GradeExportDTO> exportData = new ArrayList<>();
    int index = 1;

    for (AssignmentSubmit submit : submitList) {
      GradeExportDTO dto = new GradeExportDTO();

      // 序号
      dto.setIndex(index++);

      // 学生信息
      User student = userService.getById(submit.getUserId());
      if (student != null) {
        dto.setStudentNo(student.getUserAccount());
        dto.setStudentName(student.getUserName());
      }

      // 作业信息
      dto.setAssignmentTitle(assignment.getTitle());
      dto.setTotalScore(assignment.getTotalScore());
      dto.setPassScore(assignment.getPassScore());

      // 得分信息
      dto.setScore(submit.getTotalScore());
      dto.setIsPassed(submit.getTotalScore() >= assignment.getPassScore() ? "及格" : "不及格");

      // 时间信息
      if (submit.getSubmitTime() != null) {
        dto.setSubmitTime(DateUtil.formatDateTime(submit.getSubmitTime()));
      }
      if (submit.getGradedTime() != null) {
        dto.setGradedTime(DateUtil.formatDateTime(submit.getGradedTime()));
      }

      // 教师信息
      if (submit.getGradedBy() != null) {
        User teacher = userService.getById(submit.getGradedBy());
        if (teacher != null) {
          dto.setTeacherName(teacher.getUserName());
        }
      }

      // 评语
      dto.setComment(submit.getComment());

      exportData.add(dto);
    }

    // 设置响应头
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");

    // 文件名：作业标题_成绩单
    String fileName = assignment.getTitle() + "_成绩单";
    String encodedFileName;
    try {
      encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
    } catch (java.io.UnsupportedEncodingException e) {
      encodedFileName = fileName;
    }
    response.setHeader(
        "Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

    // 写入Excel
    try {
      EasyExcel.write(response.getOutputStream(), GradeExportDTO.class)
          .sheet("成绩单")
          .doWrite(exportData);
    } catch (IOException e) {
      log.error("导出成绩单失败", e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "导出成绩单失败");
    }
  }
}
