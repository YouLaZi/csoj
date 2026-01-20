package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 评论表 @TableName comment */
@TableName(value = "comment")
@Data
public class Comment implements Serializable {
  /** id */
  @TableId(type = IdType.ASSIGN_ID)
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

  /** 评论状态（0-正常，1-被删除） */
  private Integer status;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
