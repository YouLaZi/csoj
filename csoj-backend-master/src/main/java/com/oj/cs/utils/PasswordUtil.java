package com.oj.cs.utils;

import org.mindrot.jbcrypt.BCrypt;

import lombok.extern.slf4j.Slf4j;

/**
 * 密码加密工具类 使用 BCrypt 算法进行密码加密和验证
 *
 * <p>BCrypt 特点： 1. 每次加密结果不同（自动加盐） 2. 加密强度可配置（cost 参数） 3. 单向加密，无法解密 4. 适合密码存储场景
 *
 * @author CSOJ
 * @since 2026-01-17
 */
@Slf4j
public class PasswordUtil {

  /** 默认加密强度（cost factor） 范围：4-31，默认 10 值越大，加密越安全，但耗时越长 推荐：10-12 之间 */
  private static final int DEFAULT_COST = 10;

  /**
   * 加密密码 每次调用会生成不同的哈希值（因为盐值随机）
   *
   * @param plainPassword 明文密码
   * @return 加密后的密码（60 字符）
   */
  public static String hash(String plainPassword) {
    if (plainPassword == null || plainPassword.isEmpty()) {
      throw new IllegalArgumentException("密码不能为空");
    }
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(DEFAULT_COST));
  }

  /**
   * 加密密码（自定义加密强度）
   *
   * @param plainPassword 明文密码
   * @param cost 加密强度（4-31）
   * @return 加密后的密码
   */
  public static String hash(String plainPassword, int cost) {
    if (plainPassword == null || plainPassword.isEmpty()) {
      throw new IllegalArgumentException("密码不能为空");
    }
    if (cost < 4 || cost > 31) {
      throw new IllegalArgumentException("加密强度必须在 4-31 之间");
    }
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt(cost));
  }

  /**
   * 验证密码
   *
   * @param plainPassword 明文密码
   * @param hashedPassword 加密后的密码
   * @return 是否匹配
   */
  public static boolean verify(String plainPassword, String hashedPassword) {
    if (plainPassword == null || hashedPassword == null) {
      return false;
    }
    try {
      return BCrypt.checkpw(plainPassword, hashedPassword);
    } catch (Exception e) {
      log.error("密码验证失败", e);
      return false;
    }
  }

  /**
   * 检查密码是否已使用 BCrypt 加密 BCrypt 哈希值固定 60 字符，以 $2a$ 或 $2b$ 开头
   *
   * @param hashedPassword 待检查的密码
   * @return 是否为 BCrypt 哈希值
   */
  public static boolean isBCryptHash(String hashedPassword) {
    if (hashedPassword == null || hashedPassword.length() != 60) {
      return false;
    }
    return hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$");
  }

  /**
   * 生成随机盐值 一般不需要手动调用，hash() 方法会自动处理
   *
   * @return 盐值
   */
  public static String generateSalt() {
    return BCrypt.gensalt(DEFAULT_COST);
  }

  /**
   * 生成随机盐值（自定义加密强度）
   *
   * @param cost 加密强度
   * @return 盐值
   */
  public static String generateSalt(int cost) {
    if (cost < 4 || cost > 31) {
      throw new IllegalArgumentException("加密强度必须在 4-31 之间");
    }
    return BCrypt.gensalt(cost);
  }

  /**
   * 密码强度检查 检查密码是否符合安全要求
   *
   * @param password 待检查的密码
   * @return 检查结果
   */
  public static PasswordStrength checkStrength(String password) {
    if (password == null || password.isEmpty()) {
      return PasswordStrength.EMPTY;
    }

    int score = 0;

    // 长度检查
    if (password.length() >= 8) {
      score++;
    }
    if (password.length() >= 12) {
      score++;
    }

    // 包含小写字母
    if (password.matches(".*[a-z].*")) {
      score++;
    }

    // 包含大写字母
    if (password.matches(".*[A-Z].*")) {
      score++;
    }

    // 包含数字
    if (password.matches(".*\\d.*")) {
      score++;
    }

    // 包含特殊字符
    if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
      score++;
    }

    // 根据分数返回强度
    if (score <= 2) {
      return PasswordStrength.WEAK;
    } else if (score <= 4) {
      return PasswordStrength.MEDIUM;
    } else {
      return PasswordStrength.STRONG;
    }
  }

  /** 密码强度枚举 */
  public enum PasswordStrength {
    EMPTY, // 空密码
    WEAK, // 弱密码
    MEDIUM, // 中等强度
    STRONG // 强密码
  }
}
