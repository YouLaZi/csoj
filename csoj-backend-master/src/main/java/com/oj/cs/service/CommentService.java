package com.oj.cs.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.comment.CommentAddRequest;
import com.oj.cs.model.dto.comment.CommentQueryRequest;
import com.oj.cs.model.entity.Comment;
import com.oj.cs.model.vo.CommentVO;

/** 评论服务 */
public interface CommentService extends IService<Comment> {

  /**
   * 校验评论
   *
   * @param comment 评论
   * @param add 是否为创建校验
   */
  void validComment(Comment comment, boolean add);

  /**
   * 获取查询条件
   *
   * @param commentQueryRequest 评论查询请求
   * @return 查询条件
   */
  QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

  /**
   * 获取评论封装
   *
   * @param comment 评论
   * @param request HTTP请求
   * @return 评论封装
   */
  CommentVO getCommentVO(Comment comment, HttpServletRequest request);

  /**
   * 分页获取评论封装
   *
   * @param commentPage 评论分页
   * @param request HTTP请求
   * @return 评论分页封装
   */
  Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request);

  /**
   * 获取评论列表
   *
   * @param commentQueryRequest 评论查询请求
   * @param request HTTP请求
   * @return 评论列表
   */
  List<CommentVO> listCommentVO(
      CommentQueryRequest commentQueryRequest, HttpServletRequest request);

  /**
   * 分页获取评论列表
   *
   * @param commentQueryRequest 评论查询请求
   * @param request HTTP请求
   * @return 评论分页列表
   */
  Page<CommentVO> listCommentVOByPage(
      CommentQueryRequest commentQueryRequest, HttpServletRequest request);

  /**
   * 分页获取评论
   *
   * @param commentQueryRequest 评论查询请求
   * @return 评论分页
   */
  Page<Comment> getCommentPage(CommentQueryRequest commentQueryRequest);

  /**
   * 添加评论
   *
   * @param commentAddRequest 评论添加请求
   * @param userId 用户ID
   * @return 评论ID
   */
  long addComment(CommentAddRequest commentAddRequest, Long userId);

  /**
   * 点赞评论
   *
   * @param commentId 评论ID
   * @param userId 用户ID
   * @return 是否成功
   */
  boolean likeComment(Long commentId, Long userId);
}
