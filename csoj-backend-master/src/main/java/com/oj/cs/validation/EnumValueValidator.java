package com.oj.cs.validation;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** 枚举值校验器 */
public class EnumValueValidator implements ConstraintValidator<ValidEnum, String> {

  private List<String> enumValues = new ArrayList<>();
  private boolean ignoreCase = true;

  @Override
  public void initialize(ValidEnum constraintAnnotation) {
    Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
    this.ignoreCase = constraintAnnotation.ignoreCase();

    // 获取枚举的所有值
    Enum<?>[] enumConstants = enumClass.getEnumConstants();
    if (enumConstants != null) {
      for (Enum<?> enumConstant : enumConstants) {
        enumValues.add(enumConstant.name());
      }
    }
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true; // 使用 @NotNull 来校验空值
    }

    if (ignoreCase) {
      return enumValues.stream().anyMatch(e -> e.equalsIgnoreCase(value));
    } else {
      return enumValues.contains(value);
    }
  }
}
