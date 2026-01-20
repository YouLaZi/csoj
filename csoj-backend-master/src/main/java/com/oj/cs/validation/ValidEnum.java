package com.oj.cs.validation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** 枚举值校验注解 用于校验字符串值是否属于指定枚举的有效值 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {

  /** 校验失败时的默认消息 */
  String message() default "枚举值不合法";

  /** 分组 */
  Class<?>[] groups() default {};

  /** 负载 */
  Class<? extends Payload>[] payload() default {};

  /** 枚举类 */
  Class<? extends Enum<?>> enumClass();

  /** 是否忽略大小写 */
  boolean ignoreCase() default true;
}
