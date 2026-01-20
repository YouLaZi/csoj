package com.oj.cs.model.dto.learning;

import java.io.Serializable;
import java.util.Date;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 学习报告生成请求 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LearningReportGenerateRequest extends PageRequest implements Serializable {

  /** 报告类型 */
  private String reportType;

  /** 开始日期 */
  private Date startDate;

  /** 结束日期 */
  private Date endDate;

  private static final long serialVersionUID = 1L;
}
