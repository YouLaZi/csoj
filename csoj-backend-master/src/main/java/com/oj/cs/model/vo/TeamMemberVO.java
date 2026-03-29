package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/** 团队成员视图对象 */
@Data
public class TeamMemberVO implements Serializable {

  /** 用户ID */
  private Long userId;

  /** 用户名 */
  private String userName;

  /** 用户头像 */
  private String userAvatar;

  /** 角色：leader/vice_leader/member */
  private String role;

  /** 角色描述 */
  private String roleDesc;

  /** 加入时间 */
  private Date joinTime;

  /** 贡献积分 */
  private Integer contributionScore;

  /** 解题数 */
  private Integer solvedCount;

  /** 参赛次数 */
  private Integer contestCount;

  private static final long serialVersionUID = 1L;
}
