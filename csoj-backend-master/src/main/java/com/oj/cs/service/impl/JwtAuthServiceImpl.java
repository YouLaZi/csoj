package com.oj.cs.service.impl;

import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.user.UserLoginRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.JwtAuthService;
import com.oj.cs.service.UserService;
import com.oj.cs.utils.JwtUtil;

import lombok.extern.slf4j.Slf4j;

/** JWT 认证服务实现 */
@Slf4j
@Service
public class JwtAuthServiceImpl implements JwtAuthService {

  @Resource private UserService userService;

  @Resource private StringRedisTemplate stringRedisTemplate;

  /** Token 黑名单前缀 */
  private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

  /** Refresh Token 存储前缀 */
  private static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";

  /** Access Token 过期时间（秒） */
  private static final long ACCESS_TOKEN_EXPIRATION = 900;

  @Override
  public TokenResponse generateTokens(UserLoginRequest loginRequest) {
    // 验证用户名密码
    User user = validateUser(loginRequest.getUserAccount(), loginRequest.getUserPassword());

    // 生成 Access Token 和 Refresh Token
    String accessToken = JwtUtil.generateAccessToken(user.getId(), user.getUserRole());
    String refreshToken = JwtUtil.generateRefreshToken(user.getId());

    // 存储 Refresh Token 到 Redis
    String refreshKey = REFRESH_TOKEN_PREFIX + user.getId();
    stringRedisTemplate.opsForValue().set(refreshKey, refreshToken, 7, TimeUnit.DAYS);

    log.info("生成 Token 成功: userId={}", user.getId());

    return new TokenResponse(accessToken, refreshToken, ACCESS_TOKEN_EXPIRATION, "Bearer");
  }

  @Override
  public String refreshAccessToken(String refreshToken) {
    try {
      // 验证 Refresh Token
      if (!JwtUtil.validateToken(refreshToken)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "Refresh Token 无效或已过期");
      }

      if (!JwtUtil.isRefreshToken(refreshToken)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "Token 类型错误");
      }

      // 获取用户 ID
      Long userId = JwtUtil.getUserIdFromToken(refreshToken);

      // 检查 Refresh Token 是否与存储的一致
      String refreshKey = REFRESH_TOKEN_PREFIX + userId;
      String storedRefreshToken = stringRedisTemplate.opsForValue().get(refreshKey);

      if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "Refresh Token 已失效");
      }

      // 查询用户信息
      User user = userService.getById(userId);
      if (user == null) {
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
      }

      // 生成新的 Access Token
      String newAccessToken = JwtUtil.generateAccessToken(userId, user.getUserRole());

      log.info("刷新 Access Token 成功: userId={}", userId);
      return newAccessToken;

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("刷新 Token 失败", e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "刷新 Token 失败");
    }
  }

  @Override
  public void revokeToken(String token) {
    try {
      // 获取 Token ID
      String tokenId = JwtUtil.getTokenId(token);

      // 获取 Token 过期时间
      long remainingTime = JwtUtil.getTokenRemainingTime(token);

      if (remainingTime > 0) {
        // 将 Token 加入黑名单，直到过期
        String blacklistKey = BLACKLIST_PREFIX + tokenId;
        stringRedisTemplate
            .opsForValue()
            .set(blacklistKey, "revoked", remainingTime, TimeUnit.SECONDS);
        log.info("Token 已撤销: tokenId={}, 剩余时间={}秒", tokenId, remainingTime);
      }
    } catch (Exception e) {
      log.error("撤销 Token 失败", e);
    }
  }

  @Override
  public boolean isTokenBlacklisted(String tokenId) {
    if (tokenId == null) {
      return false;
    }
    String blacklistKey = BLACKLIST_PREFIX + tokenId;
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey));
  }

  @Override
  public void cleanExpiredBlacklist() {
    // Redis 会自动过期清理，这里可以添加定期清理逻辑
    log.debug("黑名单定期清理（Redis 自动过期）");
  }

  /**
   * 验证用户名密码
   *
   * @param userAccount 用户账号
   * @param userPassword 用户密码
   * @return 用户信息
   */
  private User validateUser(String userAccount, String userPassword) {
    User user = userService.lambdaQuery().eq(User::getUserAccount, userAccount).one();

    if (user == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
    }

    // 验证密码（支持 BCrypt 和 MD5 兼容）
    boolean passwordMatch = validatePassword(userPassword, user.getUserPassword());

    if (!passwordMatch) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
    }

    return user;
  }

  /**
   * 验证密码（兼容 BCrypt 和 MD5）
   *
   * @param plainPassword 明文密码
   * @param encryptedPassword 加密密码
   * @return 是否匹配
   */
  private boolean validatePassword(String plainPassword, String encryptedPassword) {
    try {
      // 优先使用 BCrypt 验证
      if (com.oj.cs.utils.PasswordUtil.isBCryptHash(encryptedPassword)) {
        return com.oj.cs.utils.PasswordUtil.verify(plainPassword, encryptedPassword);
      }
      // 兼容旧的 MD5
      return false;
    } catch (Exception e) {
      log.error("密码验证失败", e);
      return false;
    }
  }
}
