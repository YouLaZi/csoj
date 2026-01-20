package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/** 比赛参与者视图对象 */
@Data
public class ContestParticipantVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 参与者ID */
  private Long id;

  /** 比赛ID */
  private Long contestId;

  /** 比赛标题 */
  private String contestTitle;

  /** 用户ID */
  private Long userId;

  /** 用户姓名 */
  private String userName;

  /** 用户账号 */
  private String userAccount;

  /** 参与时间 */
  private Date joinTime;

  /** 总得分 */
  private Integer totalScore;

  /** 通过题目数 */
  private Integer passedCount;

  /** 总用时（秒） */
  private Long totalTime;

  /** 最后提交时间 */
  private Long lastSubmitTime;
}
