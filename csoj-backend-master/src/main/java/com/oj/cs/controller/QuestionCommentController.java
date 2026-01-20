package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.DeleteRequest;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.model.dto.comment.CommentAddRequest;
import com.oj.cs.model.dto.comment.CommentQueryRequest;
import com.oj.cs.model.entity.Comment;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.CommentVO;
import com.oj.cs.service.CommentService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 题目评论接口 */
@RestController
@RequestMapping("/question/comment")
@Slf4j
public class QuestionCommentController {

  @Resource private CommentService commentService;

  @Resource private UserService userService;

  /**
   * 创建评论
   *
   * @param commentAddRequest 评论创建请求
   * @param request HTTP请求
   * @return 评论ID
   */
  @PostMapping("/add")
  public BaseResponse<Long> addComment(
      @RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
    if (commentAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 登录才能评论
    User loginUser = userService.getLoginUser(request);

    Comment comment = new Comment();
    BeanUtils.copyProperties(commentAddRequest, comment);
    comment.setUserId(loginUser.getId());
    comment.setObjectType("question"); // 设置为题目评论

    // 校验评论内容
    commentService.validComment(comment, true);

    boolean result = commentService.save(comment);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    return ResultUtils.success(comment.getId());
  }

  /**
   * 删除评论
   *
   * @param deleteRequest 删除请求
   * @param request HTTP请求
   * @return 是否成功
   */
  @PostMapping("/delete")
  public BaseResponse<Boolean> deleteComment(
      @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
    if (deleteRequest == null || deleteRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    User loginUser = userService.getLoginUser(request);
    long id = deleteRequest.getId();

    // 判断是否存在
    Comment comment = commentService.getById(id);
    ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);

    // 仅本人或管理员可删除
    if (!comment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    boolean result = commentService.removeById(id);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    return ResultUtils.success(true);
  }

  /**
   * 根据题目ID获取评论列表
   *
   * @param questionId 题目ID
   * @param request HTTP请求
   * @return 评论列表
   */
  @GetMapping("/list/question/{questionId}")
  public BaseResponse<List<CommentVO>> getCommentsByQuestionId(
      @PathVariable Long questionId, HttpServletRequest request) {
    if (questionId == null || questionId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    CommentQueryRequest commentQueryRequest = new CommentQueryRequest();
    commentQueryRequest.setObjectId(questionId);
    commentQueryRequest.setObjectType("question");

    List<CommentVO> commentVOList = commentService.listCommentVO(commentQueryRequest, request);
    return ResultUtils.success(commentVOList);
  }

  /**
   * 分页获取题目评论列表
   *
   * @param commentQueryRequest 评论查询请求
   * @param request HTTP请求
   * @return 评论分页列表
   */
  @PostMapping("/list/page")
  public BaseResponse<Page<CommentVO>> listCommentByPage(
      @RequestBody CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
    if (commentQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 默认查询题目评论
    commentQueryRequest.setObjectType("question");

    long current = commentQueryRequest.getCurrent();
    long size = commentQueryRequest.getPageSize();

    // 限制爬虫
    ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

    Page<CommentVO> commentVOPage =
        commentService.listCommentVOByPage(commentQueryRequest, request);
    return ResultUtils.success(commentVOPage);
  }
}
