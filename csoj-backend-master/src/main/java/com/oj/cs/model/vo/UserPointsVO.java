package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.List;

import com.oj.cs.model.entity.PointsRecord;

import lombok.Data;

/** 用户积分信息视图 */
@Data
public class UserPointsVO implements Serializable {

  /** 用户总积分 */
  private Integer totalPoints;

  /** 积分记录列表 */
  private List<PointsRecord> records;

  private static final long serialVersionUID = 1L;
}
