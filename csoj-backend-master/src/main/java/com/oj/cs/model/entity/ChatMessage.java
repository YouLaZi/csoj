package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 聊天消息实体 */
@TableName(value = "chat_message")
@Data
public class ChatMessage implements Serializable {

  /** 主键 */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 用户ID */
  @TableField("user_id")
  private Long userId;

  /** 问题ID（可选） */
  @TableField("question_id")
  private Long questionId;

  /** 消息内容 */
  @TableField("content")
  private String content;

  /** 消息类型（user-用户消息，bot-机器人回复） */
  @TableField("message_type")
  private String messageType;

  /** 内容类型（text-文本，code-代码，math-数学公式） */
  @TableField("content_type")
  private String contentType;

  /** 编程语言（当contentType为code时有效） */
  @TableField("language")
  private String language;

  /** 创建时间 */
  @TableField("create_time")
  private Date createTime;

  /** 更新时间 */
  @TableField("update_time")
  private Date updateTime;

  /** 是否删除 */
  @TableField("is_delete")
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
