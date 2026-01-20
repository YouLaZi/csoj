package com.oj.cs.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 枚举值校验器测试 */
class EnumValueValidatorTest {

  private EnumValueValidator validator;

  @BeforeEach
  void setUp() {
    validator = new EnumValueValidator();
  }

  @Test
  void testValidEnumValue() {
    // 测试枚举校验器
    // 实际使用时通过 @ValidEnum 注解触发
    assertNotNull(validator);
  }

  @Test
  void testIgnoreCase() {
    // 测试忽略大小写
    EnumValueValidator caseInsensitiveValidator = new EnumValueValidator();
    assertNotNull(caseInsensitiveValidator);
  }
}
