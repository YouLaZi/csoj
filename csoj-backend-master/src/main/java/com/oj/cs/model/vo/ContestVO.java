package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 比赛视图对象 */
@Data
public class ContestVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 比赛ID */
  private Long id;

  /** 比赛名称 */
  private String title;

  /** 比赛描述 */
  private String description;

  /** 比赛类型 */
  private String type;

  /** 关联的题目ID列表 */
  private List<Long> questionIds;

  /** 开始时间 */
  private Date startTime;

  /** 结束时间 */
  private Date endTime;

  /** 创建用户ID（组织者） */
  private Long userId;

  /** 创建用户姓名 */
  private String userName;

  /** 状态 */
  private String status;

  /** 参与人数 */
  private Integer participantCount;

  /** 是否启用排行榜 */
  private Boolean enableRanking;

  /** 是否显示实时排名 */
  private Boolean showRealTimeRanking;

  /** 封榜时间（分钟） */
  private Integer rankingFreezeMinutes;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 当前用户是否已参加 */
  private Boolean hasJoined;

  /** 比赛持续时间（分钟） */
  private Long durationMinutes;

  /** 距离开始还有多少分钟（未开始时） */
  private Long minutesToStart;

  /** 距离结束还有多少分钟（进行中时） */
  private Long minutesToEnd;

  /** 是否已封榜 */
  private Boolean isRankingFrozen;
}
