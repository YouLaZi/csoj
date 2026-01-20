package com.oj.cs.model.dto.user;

import java.io.Serializable;

import lombok.Data;

/** 邮箱请求 */
@Data
public class EmailRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String email;
}
