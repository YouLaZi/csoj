package com.oj.cs.constant;

/** 权限常量 */
public interface PermissionConstant {

  /** 用户权限 */
  String USER_READ = "user:read";

  String USER_EDIT = "user:edit";
  String USER_DELETE = "user:delete";

  /** 题目权限 */
  String QUESTION_READ = "question:read";

  String QUESTION_EDIT = "question:edit";
  String QUESTION_DELETE = "question:delete";

  /** 提交权限 */
  String SUBMIT_READ = "submit:read";

  String SUBMIT_CREATE = "submit:create";

  /** 帖子权限 */
  String POST_READ = "post:read";

  String POST_EDIT = "post:edit";
  String POST_DELETE = "post:delete";

  /** 管理员权限 */
  String ADMIN_ACCESS = "admin:access";

  /** 积分权限 */
  String POINTS_READ = "points:read";

  String POINTS_EDIT = "points:edit";
}
