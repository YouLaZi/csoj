package com.oj.cs.controller;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.annotation.AuditLog;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.dto.user.UserLoginRequest;
import com.oj.cs.service.JwtAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/** JWT 认证接口 处理登录、Token 刷新等操作 */
@Tag(name = "JWT 认证管理")
@RestController
@RequestMapping("/auth")
@Slf4j
public class JwtAuthController {

  @Resource private JwtAuthService jwtAuthService;

  /**
   * 用户登录（返回 JWT Token）
   *
   * @param loginRequest 登录请求
   * @return Token 响应
   */
  @Operation(summary = "用户登录")
  @PostMapping("/login")
  @AuditLog(module = "认证管理", operationType = "LOGIN", description = "JWT 登录")
  public BaseResponse<JwtAuthService.TokenResponse> login(
      @RequestBody UserLoginRequest loginRequest) {
    JwtAuthService.TokenResponse tokenResponse = jwtAuthService.generateTokens(loginRequest);
    return ResultUtils.success(tokenResponse);
  }

  /**
   * 刷新 Access Token
   *
   * @param refreshToken Refresh Token
   * @return 新的 Access Token
   */
  @Operation(summary = "刷新 Token")
  @PostMapping("/refresh")
  public BaseResponse<String> refreshToken(@RequestParam String refreshToken) {
    String newAccessToken = jwtAuthService.refreshAccessToken(refreshToken);
    return ResultUtils.success(newAccessToken);
  }

  /**
   * 注销（撤销 Token）
   *
   * @param token 要撤销的 Token
   * @return 操作结果
   */
  @Operation(summary = "注销登录")
  @PostMapping("/logout")
  @AuditLog(module = "认证管理", operationType = "LOGOUT", description = "JWT 注销")
  public BaseResponse<Boolean> logout(@RequestHeader("Authorization") String token) {
    // 提取 Token
    String actualToken = token.replace("Bearer ", "");
    jwtAuthService.revokeToken(actualToken);
    return ResultUtils.success(true);
  }

  /**
   * 获取 JWT 配置信息
   *
   * @return 配置信息
   */
  @Operation(summary = "获取 JWT 配置")
  @GetMapping("/info")
  public BaseResponse<JwtInfo> getJwtInfo() {
    JwtInfo info = new JwtInfo();
    info.setTokenType("Bearer");
    info.setAccessTokenExpiration(900L);
    info.setRefreshTokenExpiration(604800L);
    info.setHeader("Authorization");
    info.setPrefix("Bearer ");
    return ResultUtils.success(info);
  }

  /** JWT 配置信息 */
  public static class JwtInfo {
    private String tokenType;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;
    private String header;
    private String prefix;

    public String getTokenType() {
      return tokenType;
    }

    public void setTokenType(String tokenType) {
      this.tokenType = tokenType;
    }

    public Long getAccessTokenExpiration() {
      return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Long accessTokenExpiration) {
      this.accessTokenExpiration = accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
      return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
      this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getHeader() {
      return header;
    }

    public void setHeader(String header) {
      this.header = header;
    }

    public String getPrefix() {
      return prefix;
    }

    public void setPrefix(String prefix) {
      this.prefix = prefix;
    }
  }
}
