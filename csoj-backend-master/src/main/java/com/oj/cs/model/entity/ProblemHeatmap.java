package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 刷题热力图 */
@TableName(value = "problem_heatmap")
@Data
public class ProblemHeatmap implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 日期 */
  private Date date;

  /** 解决问题数 */
  private Integer solvedCount;

  /** 提交次数 */
  private Integer submitCount;

  /** 花费时间(分钟) */
  private Integer timeSpent;

  /** 难度分布JSON */
  private String difficulty;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
