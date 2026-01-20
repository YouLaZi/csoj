package com.oj.cs.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.util.StringUtils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 工具类 用于生成、解析和验证 JWT 令牌
 *
 * @author CSOJ
 * @since 2026-01-17
 */
@Slf4j
public class JwtUtil {

  /** Token 类型 */
  public enum TokenType {
    ACCESS_TOKEN,
    REFRESH_TOKEN
  }

  /** 默认密钥（应在配置文件中覆盖） */
  private static final String DEFAULT_SECRET =
      "CSOJ-Default-Secret-Key-Please-Change-In-Production-Environment";

  /** 默认签发者 */
  private static final String DEFAULT_ISSUER = "csoj";

  /** 默认 Access Token 过期时间（15 分钟） */
  private static final long DEFAULT_ACCESS_EXPIRATION = 15 * 60 * 1000;

  /** 默认 Refresh Token 过期时间（7 天） */
  private static final long DEFAULT_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

  /** Token 前缀 */
  public static final String TOKEN_PREFIX = "Bearer ";

  /**
   * 生成 JWT 密钥
   *
   * @param secret 密钥字符串
   * @return SecretKey
   */
  private static SecretKey getSignKey(String secret) {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    // 确保密钥至少 256 位（32 字节）
    if (keyBytes.length < 32) {
      // 如果密钥太短，使用 HMAC-SHA256 扩展
      keyBytes = (secret + DEFAULT_SECRET).repeat(2).getBytes(StandardCharsets.UTF_8);
    }
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * 生成 Access Token
   *
   * @param userId 用户 ID
   * @param userRole 用户角色
   * @return JWT Token
   */
  public static String generateAccessToken(Long userId, String userRole) {
    return generateToken(userId, userRole, TokenType.ACCESS_TOKEN, DEFAULT_SECRET);
  }

  /**
   * 生成 Refresh Token
   *
   * @param userId 用户 ID
   * @return JWT Token
   */
  public static String generateRefreshToken(Long userId) {
    return generateToken(userId, null, TokenType.REFRESH_TOKEN, DEFAULT_SECRET);
  }

  /**
   * 生成 Token（带密钥）
   *
   * @param userId 用户 ID
   * @param userRole 用户角色
   * @param type Token 类型
   * @param secret 密钥
   * @return JWT Token
   */
  public static String generateToken(Long userId, String userRole, TokenType type, String secret) {
    long now = System.currentTimeMillis();
    long expiration =
        (type == TokenType.ACCESS_TOKEN) ? DEFAULT_ACCESS_EXPIRATION : DEFAULT_REFRESH_EXPIRATION;

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("type", type.name());
    if (userRole != null) {
      claims.put("userRole", userRole);
    }

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(String.valueOf(userId))
        .setIssuer(DEFAULT_ISSUER)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + expiration))
        .signWith(getSignKey(secret), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * 解析 Token
   *
   * @param token JWT Token
   * @return Claims
   */
  public static Claims parseToken(String token) {
    return parseToken(token, DEFAULT_SECRET);
  }

  /**
   * 解析 Token（带密钥）
   *
   * @param token JWT Token
   * @param secret 密钥
   * @return Claims
   */
  public static Claims parseToken(String token, String secret) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSignKey(secret))
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      log.warn("Token 已过期: {}", e.getMessage());
      throw new RuntimeException("Token 已过期", e);
    } catch (UnsupportedJwtException e) {
      log.warn("Token 格式不支持: {}", e.getMessage());
      throw new RuntimeException("Token 格式错误", e);
    } catch (MalformedJwtException e) {
      log.warn("Token 格式错误: {}", e.getMessage());
      throw new RuntimeException("Token 格式错误", e);
    } catch (IllegalArgumentException e) {
      log.warn("Token 为空: {}", e.getMessage());
      throw new RuntimeException("Token 不能为空", e);
    } catch (Exception e) {
      log.error("Token 解析失败", e);
      throw new RuntimeException("Token 无效", e);
    }
  }

  /**
   * 验证 Token
   *
   * @param token JWT Token
   * @return 是否有效
   */
  public static boolean validateToken(String token) {
    return validateToken(token, DEFAULT_SECRET);
  }

  /**
   * 验证 Token（带密钥）
   *
   * @param token JWT Token
   * @param secret 密钥
   * @return 是否有效
   */
  public static boolean validateToken(String token, String secret) {
    try {
      Claims claims = parseToken(token, secret);
      Date expiration = claims.getExpiration();
      return expiration.after(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 检查 Token 是否过期
   *
   * @param token JWT Token
   * @return 是否过期
   */
  public static boolean isTokenExpired(String token) {
    try {
      Claims claims = parseToken(token);
      Date expiration = claims.getExpiration();
      return expiration.before(new Date());
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * 从 Token 中获取用户 ID
   *
   * @param token JWT Token
   * @return 用户 ID
   */
  public static Long getUserIdFromToken(String token) {
    Claims claims = parseToken(token);
    return Long.valueOf(claims.getSubject());
  }

  /**
   * 从 Token 中获取用户角色
   *
   * @param token JWT Token
   * @return 用户角色
   */
  public static String getUserRoleFromToken(String token) {
    Claims claims = parseToken(token);
    return claims.get("userRole", String.class);
  }

  /**
   * 从 Token 中获取 Token 类型
   *
   * @param token JWT Token
   * @return Token 类型
   */
  public static TokenType getTokenType(String token) {
    Claims claims = parseToken(token);
    String type = claims.get("type", String.class);
    return TokenType.valueOf(type);
  }

  /**
   * 从 Token 中获取 Token ID（jti）
   *
   * @param token JWT Token
   * @return Token ID
   */
  public static String getTokenId(String token) {
    Claims claims = parseToken(token);
    return claims.getId();
  }

  /**
   * 获取 Token 过期时间
   *
   * @param token JWT Token
   * @return 过期时间
   */
  public static Date getExpirationDateFromToken(String token) {
    Claims claims = parseToken(token);
    return claims.getExpiration();
  }

  /**
   * 从请求头中提取 Token
   *
   * @param authHeader Authorization 头部值
   * @return Token，如果不合法返回 null
   */
  public static String extractToken(String authHeader) {
    if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
      return authHeader.substring(TOKEN_PREFIX.length());
    }
    return null;
  }

  /**
   * 检查 Token 是否为 Access Token
   *
   * @param token JWT Token
   * @return 是否为 Access Token
   */
  public static boolean isAccessToken(String token) {
    try {
      return getTokenType(token) == TokenType.ACCESS_TOKEN;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 检查 Token 是否为 Refresh Token
   *
   * @param token JWT Token
   * @return 是否为 Refresh Token
   */
  public static boolean isRefreshToken(String token) {
    try {
      return getTokenType(token) == TokenType.REFRESH_TOKEN;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 获取 Token 剩余有效时间（秒）
   *
   * @param token JWT Token
   * @return 剩余秒数，-1 表示已过期或无效
   */
  public static long getTokenRemainingTime(String token) {
    try {
      Date expiration = getExpirationDateFromToken(token);
      long remaining = expiration.getTime() - System.currentTimeMillis();
      return Math.max(0, remaining / 1000);
    } catch (Exception e) {
      return -1;
    }
  }
}
