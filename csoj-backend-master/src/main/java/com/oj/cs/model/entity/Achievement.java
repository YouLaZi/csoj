package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 成就定义 */
@TableName(value = "achievement")
@Data
public class Achievement implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 成就代码 */
  private String achievementCode;

  /** 成就名称 */
  private String achievementName;

  /** 成就描述 */
  private String description;

  /** 成就分类 (problem/streak/social/special/cat) */
  private String category;

  /** 图标emoji */
  private String icon;

  /** 徽章渐变CSS */
  private String gradient;

  /** 达成条件数值 */
  private Integer requirement;

  /** 奖励积分 */
  private Integer points;

  /** 稀有度 (common/rare/epic/legendary) */
  private String rarity;

  /** 是否隐藏成就 */
  private Integer isHidden;

  /** 排序顺序 */
  private Integer sortOrder;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
