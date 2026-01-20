package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 许可证管理实体 */
@TableName(value = "license")
@Data
public class License implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 许可证密钥 */
  private String licenseKey;

  /** 许可证类型（TRIAL-试用, STANDARD-标准, PREMIUM-高级, ENTERPRISE-企业） */
  private String licenseType;

  /** 学校/机构名称 */
  private String schoolName;

  /** 联系人 */
  private String contactPerson;

  /** 联系邮箱 */
  private String contactEmail;

  /** 联系电话 */
  private String contactPhone;

  /** 最大用户数 */
  private Integer maxUsers;

  /** 当前用户数 */
  private Integer currentUsers;

  /** 最大题目数 */
  private Integer maxQuestions;

  /** 是否启用比赛功能 */
  private Integer enableContest;

  /** 是否启用查重功能 */
  private Integer enablePlagiarism;

  /** 启用的功能列表（JSON格式） */
  private String features;

  /** 生效日期 */
  private Date startDate;

  /** 到期日期 */
  private Date endDate;

  /** 是否激活（0-未激活,1-已激活,2-已过期） */
  private Integer isActive;

  /** 激活时间 */
  private Date activationTime;

  /** 最后检查时间 */
  private Date lastCheckTime;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 许可证类型枚举 */
  public enum LicenseType {
    /** 试用版 */
    TRIAL,
    /** 标准版 */
    STANDARD,
    /** 高级版 */
    PREMIUM,
    /** 企业版 */
    ENTERPRISE
  }

  /** 许可证状态枚举 */
  public enum LicenseStatus {
    /** 未激活 */
    INACTIVE(0),
    /** 已激活 */
    ACTIVE(1),
    /** 已过期 */
    EXPIRED(2);

    private final int value;

    LicenseStatus(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }
}
