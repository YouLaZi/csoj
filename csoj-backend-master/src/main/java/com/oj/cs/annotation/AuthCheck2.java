package com.oj.cs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 增强版权限校验注解 支持角色和权限两种校验方式 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck2 {

  /**
   * 必须有某个角色
   *
   * @return 角色字符串
   */
  String mustRole() default "";

  /**
   * 必须有某个权限
   *
   * @return 权限字符串数组
   */
  String[] mustPermissions() default {};

  /**
   * 校验逻辑，默认必须满足所有权限 ANY: 满足任一权限即可 ALL: 必须满足所有权限
   *
   * @return 校验逻辑
   */
  String logic() default "ALL";
}
