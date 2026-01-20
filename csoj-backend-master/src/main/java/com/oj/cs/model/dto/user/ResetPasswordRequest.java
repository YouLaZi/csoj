package com.oj.cs.model.dto.user;

import java.io.Serializable;

import lombok.Data;

/** 重置密码请求 */
@Data
public class ResetPasswordRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String token;

  private String newPassword;
}
