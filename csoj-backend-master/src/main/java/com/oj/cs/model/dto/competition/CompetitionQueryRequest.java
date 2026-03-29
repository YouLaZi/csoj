package com.oj.cs.model.dto.competition;

import java.io.Serializable;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 比赛查询请求 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionQueryRequest extends PageRequest implements Serializable {

  /** 比赛名称 */
  private String name;

  /** 比赛类型 */
  private String type;

  /** 比赛状态: 0-未开始, 1-报名中, 2-进行中, 3-已结束 */
  private Integer status;

  /** 是否公开 */
  private Integer isPublic;

  private static final long serialVersionUID = 1L;
}
