package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 比赛参与者 */
@TableName(value = "contest_participant")
@Data
public class ContestParticipant implements Serializable {

  /** id */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 比赛ID */
  private Long contestId;

  /** 用户ID */
  private Long userId;

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

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  private static final long serialVersionUID = 1L;
}
