package com.oj.cs.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 用户统计缓存 */
@TableName(value = "user_stats_cache")
@Data
public class UserStatsCache implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 总解决数 */
  private Integer totalSolved;

  /** 总提交数 */
  private Integer totalSubmissions;

  /** 通过率 */
  private BigDecimal acceptRate;

  /** 当前连胜 */
  private Integer currentStreak;

  /** 最大连胜 */
  private Integer maxStreak;

  /** 平均每题时间(分钟) */
  private Integer avgTimePerProblem;

  /** 技能分数JSON {"dp": 80, "array": 90} */
  private String skillScores;

  /** 周统计JSON */
  private String weeklyStats;

  /** 月统计JSON */
  private String monthlyStats;

  /** 最后更新时间 */
  private Date lastUpdated;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
