package com.oj.cs.oauth;

import lombok.Data;

/** 统一的 OAuth 用户信息 */
@Data
public class OAuthUserInfo {

  /** 平台类型 */
  private String platform;

  /** 第三方平台的用户ID */
  private String platformUserId;

  /** 平台 openId (QQ/微信) */
  private String openId;

  /** 统一ID (微信开放平台 unionId) */
  private String unionId;

  /** 昵称 */
  private String nickname;

  /** 头像URL */
  private String avatar;

  /** 邮箱 */
  private String email;

  /** 原始响应JSON */
  private String rawJson;
}
