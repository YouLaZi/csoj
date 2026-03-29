package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 比赛对阵视图对象 */
@Data
public class MatchVO implements Serializable {

  /** 对阵ID */
  private Long id;

  /** 比赛ID */
  private Long competitionId;

  /** 轮次 */
  private Integer round;

  /** 场次 */
  private Integer matchNumber;

  /** 队伍A ID */
  private Long teamAId;

  /** 队伍A信息 */
  private TeamVO teamA;

  /** 队伍A得分 */
  private Integer teamAScore;

  /** 队伍B ID */
  private Long teamBId;

  /** 队伍B信息 */
  private TeamVO teamB;

  /** 队伍B得分 */
  private Integer teamBScore;

  /** 胜者ID */
  private Long winnerId;

  /** 对阵状态: 0-未开始, 1-进行中, 2-已结束 */
  private Integer status;

  /** 开始时间 */
  private Date startTime;

  /** 结束时间 */
  private Date endTime;

  /** 参与者列表 */
  private List<MatchParticipantVO> participants;

  private static final long serialVersionUID = 1L;
}
