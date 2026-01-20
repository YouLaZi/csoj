package com.oj.cs.service.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.oj.cs.config.properties.CorsProperties;
import com.oj.cs.service.DomainWhitelistService;

import lombok.extern.slf4j.Slf4j;

/** 域名白名单服务实现 */
@Slf4j
@Service
public class DomainWhitelistServiceImpl implements DomainWhitelistService {

  @Resource private CorsProperties corsProperties;

  @Resource private Environment environment;

  @Override
  public boolean isDomainAllowed(String origin) {
    return corsProperties.isOriginAllowed(origin);
  }

  @Override
  public List<String> getWhitelistedDomains() {
    return new ArrayList<>(corsProperties.getAllowedOrigins());
  }

  @Override
  public CorsProperties getCorsProperties() {
    return corsProperties;
  }

  @Override
  public SecurityCheckResult checkSecurity() {
    List<String> warnings = new ArrayList<>();
    List<String> allowedOrigins = corsProperties.getAllowedOrigins();
    boolean secure = true;
    String message = "CORS 配置安全";

    // 检查1：是否包含通配符 *
    if (allowedOrigins.contains("*")) {
      warnings.add("⚠️ CORS 配置包含通配符 *，允许所有域名访问（不安全）");
      secure = false;
    }

    // 检查2：是否包含 localhost
    for (String origin : allowedOrigins) {
      if (origin.contains("localhost") || origin.contains("127.0.0.1")) {
        warnings.add("⚠️ CORS 配置包含 localhost/127.0.0.1，生产环境应移除");
        break;
      }
    }

    // 检查3：是否允许凭证
    if (corsProperties.getAllowCredentials()) {
      // 如果允许凭证，检查是否有通配符域名
      for (String origin : allowedOrigins) {
        if (origin.contains("*") && !origin.equals("*")) {
          warnings.add("⚠️ allowCredentials=true 时不应使用通配符域名模式（如 https://*.edu.cn）");
          secure = false;
          break;
        }
      }
    }

    // 检查4：域名列表是否为空
    if (allowedOrigins.isEmpty()) {
      warnings.add("⚠️ CORS 允许域名列表为空，可能无法正常访问");
      secure = false;
    }

    // 检查5：是否使用 HTTP（生产环境）
    for (String origin : allowedOrigins) {
      if (origin.startsWith("http://")
          && !origin.contains("localhost")
          && !origin.contains("127.0.0.1")) {
        warnings.add("⚠️ 生产环境不应使用 HTTP 协议，请使用 HTTPS");
      }
    }

    // 检查6：环境变量覆盖检查
    String envOrigins = environment.getProperty("cors.allowed-origins");
    if (envOrigins != null && !envOrigins.isEmpty()) {
      log.info("检测到环境变量覆盖 CORS_ALLOWED_ORIGINS: {}", envOrigins);
    }

    if (!secure) {
      message = "CORS 配置存在安全风险，请检查上述警告项";
    }

    return new SecurityCheckResult(secure, message, warnings);
  }
}
