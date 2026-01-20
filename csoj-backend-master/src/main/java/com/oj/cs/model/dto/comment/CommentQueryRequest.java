package com.oj.cs.model.dto.comment;

import java.io.Serializable;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 评论查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQueryRequest extends PageRequest implements Serializable {

  /** 评论内容 */
  private String content;

  /** 评论创建用户 id */
  private Long userId;

  /** 被评论对象 id（题目 id 或帖子 id） */
  private Long objectId;

  /** 被评论对象类型（question/post） */
  private String objectType;

  /** 父评论 id */
  private Long parentId;

  /** 根评论 id */
  private Long rootId;

  private static final long serialVersionUID = 1L;
}
