package com.oj.cs.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.constant.CommonConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.CommentMapper;
import com.oj.cs.model.dto.comment.CommentAddRequest;
import com.oj.cs.model.dto.comment.CommentQueryRequest;
import com.oj.cs.model.entity.Comment;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.CommentVO;
import com.oj.cs.model.vo.UserVO;
import com.oj.cs.service.CommentService;
import com.oj.cs.service.UserService;
import com.oj.cs.utils.SqlUtils;

/** 评论服务实现 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService {

  private static final Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);

  @Override
  public boolean likeComment(Long commentId, Long userId) {
    // TODO: Implement the logic for liking a comment.
    // This is a placeholder implementation.
    // You'll need to interact with the database to record the like.
    // For example, check if the user has already liked the comment,
    // then add a record to a 'comment_likes' table or update a like count on the comment.
    if (commentId == null || userId == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论ID或用户ID不能为空");
    }
    // Placeholder: Simulate a successful like operation
    LOG.info("User {} liked comment {}", userId, commentId);
    return true; // Or false if the operation fails or is not allowed
  }

  @Override
  public long addComment(CommentAddRequest commentAddRequest, Long userId) {
    if (commentAddRequest == null || userId == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }

    // 校验评论内容
    String content = commentAddRequest.getContent();
    if (StringUtils.isBlank(content)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
    }

    // 校验对象ID
    Long objectId = commentAddRequest.getObjectId();
    if (objectId == null || objectId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论对象不存在");
    }

    // 创建评论实体
    Comment comment = new Comment();
    comment.setContent(content);
    comment.setObjectId(objectId);
    comment.setObjectType(commentAddRequest.getObjectType());
    comment.setUserId(userId);
    comment.setParentId(commentAddRequest.getParentId());
    comment.setRootId(commentAddRequest.getRootId());
    comment.setReplyUserId(commentAddRequest.getReplyUserId());
    comment.setLikeCount(0);
    comment.setCreateTime(new Date());
    comment.setUpdateTime(new Date());
    comment.setIsDelete(0);

    // 保存评论
    boolean result = this.save(comment);
    if (!result) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "评论失败");
    }

    return comment.getId();
  }

  @Resource private UserService userService;

  /**
   * 校验评论
   *
   * @param comment 评论
   * @param add 是否为创建校验
   */
  @Override
  public void validComment(Comment comment, boolean add) {
    if (comment == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    String content = comment.getContent();
    Long objectId = comment.getObjectId();
    String objectType = comment.getObjectType();

    // 创建时，参数不能为空
    if (add) {
      if (StringUtils.isAnyBlank(content, objectType) || objectId == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
      }
    }
    // 有参数则校验
    if (StringUtils.isNotBlank(content) && content.length() > 8192) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容过长");
    }
  }

  /**
   * 获取查询条件
   *
   * @param commentQueryRequest 评论查询请求
   * @return 查询条件
   */
  @Override
  public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
    QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
    if (commentQueryRequest == null) {
      return queryWrapper;
    }

    String content = commentQueryRequest.getContent();
    Long userId = commentQueryRequest.getUserId();
    Long objectId = commentQueryRequest.getObjectId();
    String objectType = commentQueryRequest.getObjectType();
    Long parentId = commentQueryRequest.getParentId();
    Long rootId = commentQueryRequest.getRootId();
    String sortField = commentQueryRequest.getSortField();
    String sortOrder = commentQueryRequest.getSortOrder();

    // 拼接查询条件
    queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
    queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
    queryWrapper.eq(ObjectUtils.isNotEmpty(objectId), "objectId", objectId);
    queryWrapper.eq(StringUtils.isNotBlank(objectType), "objectType", objectType);
    queryWrapper.eq(ObjectUtils.isNotEmpty(parentId), "parentId", parentId);
    queryWrapper.eq(ObjectUtils.isNotEmpty(rootId), "rootId", rootId);
    queryWrapper.eq("isDelete", 0);
    queryWrapper.orderBy(
        SqlUtils.validSortField(sortField),
        sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
        sortField);
    return queryWrapper;
  }

  /**
   * 获取评论封装
   *
   * @param comment 评论
   * @param request HTTP请求
   * @return 评论封装
   */
  @Override
  public CommentVO getCommentVO(Comment comment, HttpServletRequest request) {
    if (comment == null) {
      return null;
    }

    CommentVO commentVO = CommentVO.objToVo(comment);

    // 1. 关联查询用户信息
    Long userId = comment.getUserId();
    User user = null;
    if (userId != null && userId > 0) {
      user = userService.getById(userId);
    }
    UserVO userVO = userService.getUserVO(user);
    commentVO.setUserVO(userVO);

    // 2. 关联查询回复用户信息
    Long replyUserId = comment.getReplyUserId();
    User replyUser = null;
    if (replyUserId != null && replyUserId > 0) {
      replyUser = userService.getById(replyUserId);
    }
    UserVO replyUserVO = userService.getUserVO(replyUser);
    commentVO.setReplyUserVO(replyUserVO);

    return commentVO;
  }

  /**
   * 分页获取评论封装
   *
   * @param commentPage 评论分页
   * @param request HTTP请求
   * @return 评论分页封装
   */
  @Override
  public Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request) {
    List<Comment> commentList = commentPage.getRecords();
    Page<CommentVO> commentVOPage =
        new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
    if (CollectionUtils.isEmpty(commentList)) {
      return commentVOPage;
    }

    // 1. 关联查询用户信息
    Set<Long> userIdSet = commentList.stream().map(Comment::getUserId).collect(Collectors.toSet());
    // 添加回复用户 id
    userIdSet.addAll(
        commentList.stream()
            .map(Comment::getReplyUserId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toSet()));
    Map<Long, List<User>> userIdUserListMap =
        userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));

    // 2. 填充信息
    List<CommentVO> commentVOList =
        commentList.stream()
            .map(
                comment -> {
                  CommentVO commentVO = CommentVO.objToVo(comment);
                  // 创建用户
                  Long userId = comment.getUserId();
                  User user = null;
                  if (userIdUserListMap.containsKey(userId)) {
                    user = userIdUserListMap.get(userId).get(0);
                  }
                  commentVO.setUserVO(userService.getUserVO(user));

                  // 回复用户
                  Long replyUserId = comment.getReplyUserId();
                  User replyUser = null;
                  if (replyUserId != null
                      && replyUserId > 0
                      && userIdUserListMap.containsKey(replyUserId)) {
                    replyUser = userIdUserListMap.get(replyUserId).get(0);
                  }
                  commentVO.setReplyUserVO(userService.getUserVO(replyUser));

                  return commentVO;
                })
            .collect(Collectors.toList());

    commentVOPage.setRecords(commentVOList);
    return commentVOPage;
  }

  /**
   * 获取评论列表
   *
   * @param commentQueryRequest 评论查询请求
   * @param request HTTP请求
   * @return 评论列表
   */
  @Override
  public List<CommentVO> listCommentVO(
      CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
    if (commentQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 构建查询条件
    QueryWrapper<Comment> queryWrapper = getQueryWrapper(commentQueryRequest);
    List<Comment> commentList = this.list(queryWrapper);

    // 获取评论VO列表
    return commentList.stream()
        .map(comment -> getCommentVO(comment, request))
        .collect(Collectors.toList());
  }

  /**
   * 分页获取评论列表
   *
   * @param commentQueryRequest 评论查询请求
   * @param request HTTP请求
   * @return 评论分页列表
   */
  @Override
  public Page<CommentVO> listCommentVOByPage(
      CommentQueryRequest commentQueryRequest, HttpServletRequest request) {
    long current = commentQueryRequest.getCurrent();
    long size = commentQueryRequest.getPageSize();

    // 限制爬虫
    if (size > 20) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 构建查询条件
    QueryWrapper<Comment> queryWrapper = getQueryWrapper(commentQueryRequest);
    Page<Comment> commentPage = this.page(new Page<>(current, size), queryWrapper);

    return getCommentVOPage(commentPage, request);
  }

  /**
   * 分页获取评论
   *
   * @param commentQueryRequest 评论查询请求
   * @return 评论分页
   */
  @Override
  public Page<Comment> getCommentPage(CommentQueryRequest commentQueryRequest) {
    if (commentQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    long current = commentQueryRequest.getCurrent();
    long size = commentQueryRequest.getPageSize();

    // 限制爬虫
    if (size > 20) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 构建查询条件
    QueryWrapper<Comment> queryWrapper = getQueryWrapper(commentQueryRequest);
    Page<Comment> commentPage = this.page(new Page<>(current, size), queryWrapper);

    return commentPage;
  }
}
