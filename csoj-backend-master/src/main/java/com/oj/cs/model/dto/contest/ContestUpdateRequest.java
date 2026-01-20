package com.oj.cs.model.dto.contest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 更新比赛请求 */
@Data
public class ContestUpdateRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 比赛ID */
  private Long id;

  /** 比赛名称 */
  private String title;

  /** 比赛描述 */
  private String description;

  /** 比赛类型 */
  private String type;

  /** 比赛密码 */
  private String password;

  /** 关联的题目ID列表 */
  private List<Long> questionIds;

  /** 开始时间 */
  private Date startTime;

  /** 结束时间 */
  private Date endTime;

  /** 状态 */
  private String status;

  /** 是否启用排行榜 */
  private Boolean enableRanking;

  /** 是否显示实时排名 */
  private Boolean showRealTimeRanking;

  /** 封榜时间（分钟） */
  private Integer rankingFreezeMinutes;
}
