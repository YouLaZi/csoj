package com.oj.cs.config.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/** JWT 配置属性 从 application.yml 读取 jwt.* 配置 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

  /** JWT 密钥 */
  private String secret = "CSOJ-Default-Secret-Key-Please-Change-In-Production-Environment";

  /** Access Token 过期时间（秒） */
  private Long accessTokenExpiration = 900L;

  /** Refresh Token 过期时间（秒） */
  private Long refreshTokenExpiration = 604800L;

  /** Token 签发者 */
  private String issuer = "csoj";

  /** Token 头部名称 */
  private String header = "Authorization";

  /** Token 前缀 */
  private String prefix = "Bearer ";

  /**
   * 检查配置是否安全
   *
   * @return 安全检查结果
   */
  public SecurityCheckResult checkSecurity() {
    List<String> warnings = new ArrayList<>();
    boolean secure = true;
    String message = "JWT 配置安全";

    // 检查1：是否使用默认密钥
    if (secret.contains("Default") || secret.contains("Please-Change")) {
      warnings.add("⚠️ JWT 使用默认密钥，生产环境必须更换！");
      secure = false;
    }

    // 检查2：密钥长度
    if (secret.length() < 32) {
      warnings.add("⚠️ JWT 密钥长度过短（建议至少 32 字符）");
      secure = false;
    }

    // 检查3：Token 过期时间
    if (accessTokenExpiration > 3600) {
      warnings.add("⚠️ Access Token 过期时间过长（建议不超过 1 小时）");
    }

    if (!secure) {
      message = "JWT 配置存在安全风险，请检查上述警告项";
    }

    return new SecurityCheckResult(secure, message, warnings);
  }

  /** 安全检查结果 */
  public static class SecurityCheckResult {
    private boolean secure;
    private String message;
    private List<String> warnings;

    public SecurityCheckResult(boolean secure, String message, List<String> warnings) {
      this.secure = secure;
      this.message = message;
      this.warnings = warnings;
    }

    public boolean isSecure() {
      return secure;
    }

    public String getMessage() {
      return message;
    }

    public List<String> getWarnings() {
      return warnings;
    }
  }
}
