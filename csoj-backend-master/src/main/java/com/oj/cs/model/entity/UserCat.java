package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 用户猫咪 */
@TableName(value = "user_cat")
@Data
public class UserCat implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 猫咪名字 */
  private String catName;

  /** 等级 (1-100) */
  private Integer level;

  /** 当前经验值 */
  private Integer experience;

  /** 饱食度 (0-100) */
  private Integer hunger;

  /** 快乐值 (0-100) */
  private Integer happiness;

  /** 健康值 (0-100) */
  private Integer health;

  /** 精力值 (0-100) */
  private Integer energy;

  /** 心情 (happy/sad/thinking/sleeping/excited/surprised) */
  private String mood;

  /** 最后喂食时间 */
  private Date lastFeedTime;

  /** 最后玩耍时间 */
  private Date lastPlayTime;

  /** 最后睡眠时间 */
  private Date lastSleepTime;

  /** 总喂食次数 */
  private Integer totalFeedCount;

  /** 总玩耍次数 */
  private Integer totalPlayCount;

  /** 猫咪创建日期 */
  private Date createDay;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
