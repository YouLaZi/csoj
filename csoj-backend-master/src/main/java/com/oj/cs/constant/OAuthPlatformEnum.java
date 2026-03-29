package com.oj.cs.constant;

import lombok.Getter;

/** OAuth 平台枚举 */
@Getter
public enum OAuthPlatformEnum {
  GITHUB("GITHUB", "GitHub", "github"),
  GITEE("GITEE", "Gitee (码云)", "gitee"),
  QQ("QQ", "QQ", "qq"),
  WECHAT("WECHAT", "微信", "wechat");

  private final String code;
  private final String name;
  private final String pathName;

  OAuthPlatformEnum(String code, String name, String pathName) {
    this.code = code;
    this.name = name;
    this.pathName = pathName;
  }

  /** 根据code获取枚举 */
  public static OAuthPlatformEnum getByCode(String code) {
    if (code == null) {
      return null;
    }
    for (OAuthPlatformEnum platform : values()) {
      if (platform.getCode().equalsIgnoreCase(code)) {
        return platform;
      }
    }
    return null;
  }

  /** 根据路径名获取枚举 */
  public static OAuthPlatformEnum getByPathName(String pathName) {
    if (pathName == null) {
      return null;
    }
    for (OAuthPlatformEnum platform : values()) {
      if (platform.getPathName().equalsIgnoreCase(pathName)) {
        return platform;
      }
    }
    return null;
  }
}
