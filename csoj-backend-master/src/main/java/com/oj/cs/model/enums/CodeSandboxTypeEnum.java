package com.oj.cs.model.enums;

import lombok.Getter;

/** 代码沙箱类型枚举 */
@Getter
public enum CodeSandboxTypeEnum {

  /** 示例代码沙箱（用于测试） */
  EXAMPLE("example", "示例代码沙箱"),

  /** 远程代码沙箱（实际调用远程接口） */
  REMOTE("remote", "远程代码沙箱"),

  /** 第三方代码沙箱（调用第三方服务） */
  THIRD_PARTY("thirdParty", "第三方代码沙箱");

  /** 类型标识 */
  private final String value;

  /** 描述信息 */
  private final String description;

  CodeSandboxTypeEnum(String value, String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * 根据值获取枚举
   *
   * @param value 类型标识
   * @return 对应的枚举，如果未找到则返回默认的REMOTE
   */
  public static CodeSandboxTypeEnum fromValue(String value) {
    if (value == null || value.isEmpty()) {
      return REMOTE;
    }
    for (CodeSandboxTypeEnum typeEnum : values()) {
      if (typeEnum.getValue().equalsIgnoreCase(value)) {
        return typeEnum;
      }
    }
    return REMOTE;
  }

  /**
   * 验证是否为有效的沙箱类型
   *
   * @param value 类型标识
   * @return 是否有效
   */
  public static boolean isValid(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    for (CodeSandboxTypeEnum typeEnum : values()) {
      if (typeEnum.getValue().equalsIgnoreCase(value)) {
        return true;
      }
    }
    return false;
  }
}
