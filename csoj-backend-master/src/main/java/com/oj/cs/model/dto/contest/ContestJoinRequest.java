package com.oj.cs.model.dto.contest;

import java.io.Serializable;

import lombok.Data;

/** 参加比赛请求 */
@Data
public class ContestJoinRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 比赛ID */
  private Long contestId;

  /** 比赛密码（type为PASSWORD时需要） */
  private String password;
}
