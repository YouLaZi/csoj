package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 猫咪物品 */
@TableName(value = "cat_item")
@Data
public class CatItem implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 物品代码 */
  private String itemCode;

  /** 物品名称 */
  private String itemName;

  /** 物品类型 (food/toy/accessory/decoration) */
  private String itemType;

  /** 物品描述 */
  private String description;

  /** 物品效果JSON {"hunger": 20, "happiness": 10} */
  private String effect;

  /** 价格(积分) */
  private Integer price;

  /** 图标emoji */
  private String icon;

  /** 稀有度 (common/rare/epic/legendary) */
  private String rarity;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
