package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 比赛对阵实体 */
@Data
@TableName("competition_match")
public class CompetitionMatch implements Serializable {

  /** 主键ID */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 比赛ID */
  private Long competitionId;

  /** 轮次 */
  private Integer round;

  /** 场次 */
  private Integer matchNumber;

  /** 队伍A ID */
  private Long teamAId;

  /** 队伍B ID */
  private Long teamBId;

  /** 队伍A得分 */
  private Integer teamAScore;

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

  /** 创建时间 */
  @TableField(fill = FieldFill.INSERT)
  private Date createTime;

  /** 更新时间 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  private static final long serialVersionUID = 1L;
}
