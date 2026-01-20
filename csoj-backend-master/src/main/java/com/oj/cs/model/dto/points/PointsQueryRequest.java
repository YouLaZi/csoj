package com.oj.cs.model.dto.points;

import java.io.Serializable;
import java.util.Date;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 积分记录查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PointsQueryRequest extends PageRequest implements Serializable {

  /** 用户ID */
  private Long userId;

  /** 积分类型 */
  private String type;

  /** 开始时间 */
  private Date startTime;

  /** 结束时间 */
  private Date endTime;

  /** 时间范围（day/week/month/all） */
  private String timeRange;

  private static final long serialVersionUID = 1L;
}
