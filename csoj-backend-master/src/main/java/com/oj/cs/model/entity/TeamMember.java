package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 团队成员实体 */
@TableName("team_member")
@Data
public class TeamMember implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;

  /** 团队ID */
  @TableField("team_id")
  private Long teamId;

  /** 用户ID */
  @TableField("user_id")
  private Long userId;

  /** 角色：leader/vice_leader/member */
  private String role;

  /** 加入时间 */
  @TableField("join_time")
  private Date joinTime;

  /** 贡献积分 */
  @TableField("contribution_score")
  private Integer contributionScore;

  /** 解题数 */
  @TableField("solved_count")
  private Integer solvedCount;

  /** 参赛次数 */
  @TableField("contest_count")
  private Integer contestCount;

  /** 是否删除 */
  @TableLogic
  @TableField("is_delete")
  private Integer isDelete;
}
