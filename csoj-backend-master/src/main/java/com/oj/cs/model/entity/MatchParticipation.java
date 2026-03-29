package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 对阵参与者实体 */
@Data
@TableName("match_participation")
public class MatchParticipation implements Serializable {

  /** 主键ID */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 对阵ID */
  private Long matchId;

  /** 团队ID */
  private Long teamId;

  /** 用户ID */
  private Long userId;

  /** 解决题目数 */
  private Integer solvedCount;

  /** 总用时(毫秒) */
  private Long totalTime;

  /** 得分 */
  private Integer score;

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
