package com.oj.cs.service;

import com.oj.cs.model.dto.user.UserLoginRequest;

/** JWT 认证服务 处理 Token 生成、刷新、撤销等操作 */
public interface JwtAuthService {

  /**
   * 生成登录 Token（Access Token + Refresh Token）
   *
   * @param loginRequest 登录请求
   * @return Token 响应
   */
  TokenResponse generateTokens(UserLoginRequest loginRequest);

  /**
   * 刷新 Access Token
   *
   * @param refreshToken Refresh Token
   * @return 新的 Access Token
   */
  String refreshAccessToken(String refreshToken);

  /**
   * 撤销 Token（加入黑名单）
   *
   * @param token 要撤销的 Token
   */
  void revokeToken(String token);

  /**
   * 检查 Token 是否在黑名单中
   *
   * @param tokenId Token ID
   * @return 是否在黑名单中
   */
  boolean isTokenBlacklisted(String tokenId);

  /** 清理过期的黑名单记录 */
  void cleanExpiredBlacklist();

  /** Token 响应 */
  class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;

    public TokenResponse(
        String accessToken, String refreshToken, Long expiresIn, String tokenType) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      this.expiresIn = expiresIn;
      this.tokenType = tokenType;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public String getRefreshToken() {
      return refreshToken;
    }

    public Long getExpiresIn() {
      return expiresIn;
    }

    public String getTokenType() {
      return tokenType;
    }
  }
}
