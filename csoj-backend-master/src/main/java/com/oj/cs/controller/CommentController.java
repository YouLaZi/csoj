package com.oj.cs.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.comment.CommentAddRequest;
import com.oj.cs.model.dto.comment.CommentQueryRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.CommentVO;
import com.oj.cs.service.CommentService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 评论接口 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

  @Resource private CommentService commentService;

  @Resource private UserService userService;

  /**
   * 获取评论列表
   *
   * @param commentQueryRequest 评论查询请求
   * @param request HTTP请求
   * @return 评论列表
   */
  @GetMapping("/list")
  public BaseResponse<Page<CommentVO>> listComments(
      CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
    if (commentQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long current = commentQueryRequest.getCurrent();
    long size = commentQueryRequest.getPageSize();
    // 限制爬虫
    if (size > 20) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Page<CommentVO> commentVOPage =
        commentService.listCommentVOByPage(commentQueryRequest, request);
    return ResultUtils.success(commentVOPage);
  }

  /**
   * 添加评论
   *
   * @param commentAddRequest 评论创建请求
   * @param request HTTP请求
   * @return 创建的评论ID
   */
  @PostMapping("/add")
  public BaseResponse<Long> addComment(
      @RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
    if (commentAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    long commentId = commentService.addComment(commentAddRequest, loginUser.getId());
    return ResultUtils.success(commentId);
  }

  /**
   * 评论点赞
   *
   * @param id 评论ID
   * @param request HTTP请求
   * @return 是否成功
   */
  @PostMapping("/like")
  public BaseResponse<Boolean> likeComment(@RequestParam Long id, HttpServletRequest request) {
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    boolean result = commentService.likeComment(id, loginUser.getId());
    return ResultUtils.success(result);
  }
}
