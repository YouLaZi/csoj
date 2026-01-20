package com.oj.cs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 操作审计日志注解 用于标记需要进行审计的方法 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

  /**
   * 操作模块
   *
   * @return 模块名称
   */
  String module() default "";

  /**
   * 操作类型
   *
   * @return 操作类型
   */
  String operationType() default "";

  /**
   * 操作描述
   *
   * @return 描述
   */
  String description() default "";
}
