package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 比赛报名实体 */
@Data
@TableName("competition_registration")
public class CompetitionRegistration implements Serializable {

  /** 主键ID */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 比赛ID */
  private Long competitionId;

  /** 团队ID */
  private Long teamId;

  /** 报名时间 */
  private Date registerTime;

  /** 报名状态: 0-待审核, 1-已确认, 2-已取消 */
  private Integer status;

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
