package com.oj.cs.config.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/** CORS 跨域配置属性 从 application.yml 读取 cors.* 配置 */
@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

  /** 允许的域名列表 支持通配符，如 https://*.edu.cn */
  private List<String> allowedOrigins = new ArrayList<>();

  /** 允许的 HTTP 方法 */
  private List<String> allowedMethods = new ArrayList<>();

  /** 允许的请求头 */
  private String allowedHeaders = "*";

  /** 是否允许发送凭证（Cookie） */
  private Boolean allowCredentials = true;

  /** 预检请求缓存时间（秒） */
  private Long maxAge = 3600L;

  /** 暴露的响应头 */
  private List<String> exposedHeaders = new ArrayList<>();

  /**
   * 检查域名是否在允许列表中 支持通配符匹配，如 https://*.edu.cn
   *
   * @param origin 待检查的域名
   * @return 是否允许
   */
  public boolean isOriginAllowed(String origin) {
    if (origin == null || origin.isEmpty()) {
      return false;
    }

    for (String allowedPattern : allowedOrigins) {
      if (matchesPattern(origin, allowedPattern)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 域名匹配（支持通配符）
   *
   * @param origin 待匹配的域名
   * @param pattern 匹配模式（支持 * 通配符）
   * @return 是否匹配
   */
  private boolean matchesPattern(String origin, String pattern) {
    if ("*".equals(pattern)) {
      return true;
    }

    if (pattern.contains("*")) {
      // 将通配符模式转换为正则表达式
      String regex = pattern.replace(".", "\\.").replace("*", ".*");
      return origin.matches(regex);
    }

    return origin.equals(pattern);
  }
}
