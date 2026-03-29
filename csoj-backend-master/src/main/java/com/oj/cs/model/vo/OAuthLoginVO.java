package com.oj.cs.model.vo;

import java.io.Serializable;

import lombok.Data;

/** OAuth 登录结果 VO */
@Data
public class OAuthLoginVO implements Serializable {

  /** 登录是否成功 */
  private boolean success;

  /** 登录成功后的 Token */
  private String token;

  /** 用户信息（登录成功时返回） */
  private LoginUserVO userInfo;

  /** 是否需要绑定账号（首次OAuth登录新用户时为true） */
  private boolean needBind;

  /** OAuth临时Token（用于绑定已有账号） */
  private String oauthToken;

  /** 第三方平台 */
  private String platform;

  /** 第三方昵称 */
  private String nickname;

  /** 第三方头像 */
  private String avatar;

  /** 错误信息 */
  private String message;
}
