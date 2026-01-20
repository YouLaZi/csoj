package com.oj.cs.model.dto.export;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 数据导出请求 */
@Data
public class DataExportRequest implements Serializable {

  /** 导出类型 */
  private String exportType;

  /** 导出格式 (excel, csv, pdf) */
  private String format;

  /** 比赛 ID（导出比赛数据时使用） */
  private Long contestId;

  /** 用户 ID（导出用户数据时使用） */
  private Long userId;

  /** 题目 ID 列表（导出题目数据时使用） */
  private List<Long> questionIds;

  /** 开始时间 */
  private String startTime;

  /** 结束时间 */
  private String endTime;

  /** 是否包含详细数据 */
  private Boolean includeDetails;

  /** 导出列配置（JSON 格式） */
  private String columns;

  private static final long serialVersionUID = 1L;
}
