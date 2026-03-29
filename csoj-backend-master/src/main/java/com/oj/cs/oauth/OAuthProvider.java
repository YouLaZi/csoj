package com.oj.cs.oauth;

/** OAuth 平台提供者接口 使用策略模式，每个平台实现此接口 */
public interface OAuthProvider {

  /** 获取平台名称 */
  String getPlatform();

  /**
   * 构建授权URL
   *
   * @param state CSRF 防护参数
   * @param redirectUri 回调URL
   * @return 授权URL
   */
  String buildAuthorizationUrl(String state, String redirectUri);

  /**
   * 使用授权码换取访问令牌
   *
   * @param code 授权码
   * @param redirectUri 回调URL
   * @return 访问令牌
   */
  String getAccessToken(String code, String redirectUri);

  /**
   * 使用访问令牌获取用户信息
   *
   * @param accessToken 访问令牌
   * @return 统一的用户信息
   */
  OAuthUserInfo getUserInfo(String accessToken);

  /**
   * 刷新访问令牌（可选实现）
   *
   * @param refreshToken 刷新令牌
   * @return 新的访问令牌
   */
  default String refreshAccessToken(String refreshToken) {
    throw new UnsupportedOperationException("该平台不支持刷新令牌");
  }

  /** 检查平台是否启用 */
  boolean isEnabled();
}
