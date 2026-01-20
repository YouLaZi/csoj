package com.oj.cs.model.dto.contest;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 创建比赛请求 */
@Data
public class ContestAddRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 比赛名称 */
  private String title;

  /** 比赛描述 */
  private String description;

  /** 比赛类型（PUBLIC-公开赛，PRIVATE-私有赛，PASSWORD-密码赛） */
  private String type;

  /** 比赛密码（type为PASSWORD时需要） */
  private String password;

  /** 关联的题目ID列表 */
  private List<Long> questionIds;

  /** 开始时间 */
  private Date startTime;

  /** 结束时间 */
  private Date endTime;

  /** 是否启用排行榜 */
  private Boolean enableRanking;

  /** 是否显示实时排名 */
  private Boolean showRealTimeRanking;

  /** 封榜时间（分钟） */
  private Integer rankingFreezeMinutes;
}
