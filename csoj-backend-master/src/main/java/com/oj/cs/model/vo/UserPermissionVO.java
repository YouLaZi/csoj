package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 用户权限视图对象 */
@Data
public class UserPermissionVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 用户ID */
  private Long userId;

  /** 用户角色 */
  private String userRole;

  /** 用户权限列表 */
  private List<String> permissions;
}
