package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 系统公告 */
@TableName(value = "announcement")
@Data
public class Announcement implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 标题 */
  private String title;

  /** 内容 */
  private String content;

  /** 公告类型（0-普通公告，1-重要公告，2-紧急公告） */
  private Integer type;

  /** 状态（0-未发布，1-已发布） */
  private Integer status;

  /** 创建用户 id */
  private Long userId;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
