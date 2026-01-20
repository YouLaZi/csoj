package com.oj.cs.model.dto.user;

import lombok.Data;

/** 学生导入 DTO 用于 Excel 批量导入学生 */
@Data
public class StudentImportDTO {

  /** 学号（必填） */
  private String studentNo;

  /** 用户名（必填，默认使用学号） */
  private String userName;

  /** 密码（必填，默认123456） */
  private String userPassword;

  /** 真实姓名（必填） */
  private String userNameReal;

  /** 邮箱（选填） */
  private String userEmail;

  /** 手机号（选填） */
  private String userPhone;

  /** 班级（选填） */
  private String className;

  /** 专业（选填） */
  private String major;

  /** 入学年份（选填，格式：2024） */
  private String admissionYear;
}
