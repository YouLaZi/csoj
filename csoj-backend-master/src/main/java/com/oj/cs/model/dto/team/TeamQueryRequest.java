package com.oj.cs.model.dto.team;

import java.io.Serializable;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 团队查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQueryRequest extends PageRequest implements Serializable {

  /** 队伍名称（模糊搜索） */
  private String name;

  /** 是否公开招募 */
  private Integer isPublic;

  /** 最小积分 */
  private Integer minRating;

  /** 最大积分 */
  private Integer maxRating;

  private static final long serialVersionUID = 1L;
}
