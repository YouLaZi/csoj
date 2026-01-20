package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;

import com.oj.cs.model.entity.Comment;

import lombok.Data;

/** 评论视图对象 */
@Data
public class CommentVO implements Serializable {

  /** id */
  private Long id;

  /** 评论内容 */
  private String content;

  /** 评论创建用户 id */
  private Long userId;

  /** 被评论对象 id（题目 id 或帖子 id） */
  private Long objectId;

  /** 被评论对象类型（question/post） */
  private String objectType;

  /** 父评论 id，为 0 则表示一级评论 */
  private Long parentId;

  /** 根评论 id，为 0 则表示一级评论 */
  private Long rootId;

  /** 回复用户 id */
  private Long replyUserId;

  /** 点赞数 */
  private Integer likeCount;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 创建评论的用户信息 */
  private UserVO userVO;

  /** 回复的用户信息 */
  private UserVO replyUserVO;

  /**
   * 对象转包装类
   *
   * @param comment 评论
   * @return 评论包装类
   */
  public static CommentVO objToVo(Comment comment) {
    if (comment == null) {
      return null;
    }
    CommentVO commentVO = new CommentVO();
    commentVO.setId(comment.getId());
    commentVO.setContent(comment.getContent());
    commentVO.setUserId(comment.getUserId());
    commentVO.setObjectId(comment.getObjectId());
    commentVO.setObjectType(comment.getObjectType());
    commentVO.setParentId(comment.getParentId());
    commentVO.setRootId(comment.getRootId());
    commentVO.setReplyUserId(comment.getReplyUserId());
    commentVO.setLikeCount(comment.getLikeCount());
    commentVO.setCreateTime(comment.getCreateTime());
    commentVO.setUpdateTime(comment.getUpdateTime());
    return commentVO;
  }

  private static final long serialVersionUID = 1L;
}
