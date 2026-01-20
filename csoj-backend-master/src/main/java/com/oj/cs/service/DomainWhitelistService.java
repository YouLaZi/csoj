package com.oj.cs.service;

import java.util.List;

import com.oj.cs.config.properties.CorsProperties;

/** 域名白名单服务 用于验证和动态管理允许访问的域名 */
public interface DomainWhitelistService {

  /**
   * 验证域名是否在白名单中
   *
   * @param origin 待验证的域名（如 https://example.com）
   * @return 是否允许
   */
  boolean isDomainAllowed(String origin);

  /**
   * 获取当前白名单中的所有域名
   *
   * @return 域名列表
   */
  List<String> getWhitelistedDomains();

  /**
   * 获取 CORS 配置属性
   *
   * @return CORS 配置
   */
  CorsProperties getCorsProperties();

  /**
   * 检查当前配置是否安全 用于生产环境安全检查
   *
   * @return 安全检查结果
   */
  SecurityCheckResult checkSecurity();

  /** 安全检查结果 */
  class SecurityCheckResult {
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
