package com.oj.cs.model.dto.comment;

import java.io.Serializable;

import lombok.Data;

/** 评论创建请求 */
@Data
public class CommentAddRequest implements Serializable {

  /** 评论内容 */
  private String content;

  /** 被评论对象 id（题目 id 或帖子 id） */
  private Long objectId;

  /** 被评论对象类型（question/post） */
  private String objectType;

  /** 父评论 id，为 0 则表示一级评论 */
  private Long parentId = 0L;

  /** 根评论 id，为 0 则表示一级评论 */
  private Long rootId = 0L;

  /** 回复用户 id */
  private Long replyUserId;

  private static final long serialVersionUID = 1L;
}
