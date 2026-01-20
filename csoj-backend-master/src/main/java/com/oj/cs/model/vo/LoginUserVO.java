package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/** 已登录用户视图（脱敏） */
@Data
public class LoginUserVO implements Serializable {

  /** 用户 id */
  private Long id;

  /** 用户昵称 */
  private String userName;

  /** 用户头像 */
  private String userAvatar;

  /** 用户简介 */
  private String userProfile;

  /** 用户角色：user/admin/ban */
  private String userRole;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 登录凭证 */
  private String token;

  /** 已解决题目数量 */
  private Integer solvedCount;

  /** 提交次数 */
  private Integer submissionCount;

  /** 用户积分 */
  private Integer points;

  private static final long serialVersionUID = 1L;
}
