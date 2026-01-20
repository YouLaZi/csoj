package com.oj.cs.model.dto.license;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/** 许可证生成/激活请求 */
@Data
public class LicenseRequest implements Serializable {

  /** 许可证密钥（激活时提供） */
  private String licenseKey;

  /** 许可证类型（生成时提供） */
  private String licenseType;

  /** 学校/机构名称 */
  private String schoolName;

  /** 联系人 */
  private String contactPerson;

  /** 联系邮箱 */
  private String contactEmail;

  /** 联系电话 */
  private String contactPhone;

  /** 最大用户数（生成时提供） */
  private Integer maxUsers;

  /** 最大题目数（生成时提供） */
  private Integer maxQuestions;

  /** 生效日期（生成时提供） */
  private Date startDate;

  /** 到期日期（生成时提供） */
  private Date endDate;

  /** 启用的功能列表（生成时提供） */
  private List<String> features;

  private static final long serialVersionUID = 1L;
}
