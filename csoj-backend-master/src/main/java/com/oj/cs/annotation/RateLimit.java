package com.oj.cs.annotation;

import java.lang.annotation.*;

/**
 * 限流注解 用于标记需要进行限流的接口
 *
 * @author CSOJ
 * @since 2026-01-17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

  /** 每秒允许的请求数 */
  double permitsPerSecond() default 10.0;

  /** 限流 key 前缀 */
  String key() default "rate_limit";

  /** 限流模式 */
  LimitMode mode() default LimitMode.USER;

  /** 限流模式枚举 */
  enum LimitMode {
    /** 基于用户 ID 限流 */
    USER,

    /** 基于 IP 限流 */
    IP,

    /** 全局限流（所有请求共享） */
    GLOBAL
  }
}
