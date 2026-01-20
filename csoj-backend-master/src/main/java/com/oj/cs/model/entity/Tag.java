package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 标签 */
@TableName(value = "tag")
@Data
public class Tag implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 标签名称 */
  private String name;

  /** 标签颜色 */
  private String color;

  /** 标签类型（0-题目标签，1-讨论区标签） */
  private Integer type;

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
