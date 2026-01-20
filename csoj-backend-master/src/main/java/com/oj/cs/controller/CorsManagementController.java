package com.oj.cs.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.config.properties.CorsProperties;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.service.DomainWhitelistService;

import lombok.extern.slf4j.Slf4j;

/** CORS 配置管理接口（仅管理员） 用于查看和管理跨域配置 */
@RestController
@RequestMapping("/admin/cors")
@Slf4j
public class CorsManagementController {

  @Resource private DomainWhitelistService domainWhitelistService;

  /**
   * 获取当前 CORS 配置
   *
   * @return CORS 配置信息
   */
  @GetMapping("/config")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Map<String, Object>> getCorsConfig() {
    CorsProperties corsProperties = domainWhitelistService.getCorsProperties();

    Map<String, Object> config = new HashMap<>();
    config.put("allowedOrigins", corsProperties.getAllowedOrigins());
    config.put("allowedMethods", corsProperties.getAllowedMethods());
    config.put("allowedHeaders", corsProperties.getAllowedHeaders());
    config.put("allowCredentials", corsProperties.getAllowCredentials());
    config.put("maxAge", corsProperties.getMaxAge());
    config.put("exposedHeaders", corsProperties.getExposedHeaders());

    log.info("管理员查询 CORS 配置");
    return ResultUtils.success(config);
  }

  /**
   * 安全检查 检查当前 CORS 配置是否安全
   *
   * @return 安全检查结果
   */
  @GetMapping("/security-check")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Map<String, Object>> checkSecurity() {
    DomainWhitelistService.SecurityCheckResult result = domainWhitelistService.checkSecurity();

    Map<String, Object> response = new HashMap<>();
    response.put("secure", result.isSecure());
    response.put("message", result.getMessage());
    response.put("warnings", result.getWarnings());

    // 记录安全检查日志
    if (!result.isSecure()) {
      log.warn("CORS 安全检查失败: {}", result.getWarnings());
    } else {
      log.info("CORS 安全检查通过");
    }

    return ResultUtils.success(response);
  }

  /**
   * 获取白名单域名列表
   *
   * @return 域名列表
   */
  @GetMapping("/whitelist")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Map<String, Object>> getWhitelist() {
    Map<String, Object> response = new HashMap<>();
    response.put("domains", domainWhitelistService.getWhitelistedDomains());
    response.put("count", domainWhitelistService.getWhitelistedDomains().size());

    return ResultUtils.success(response);
  }

  /**
   * 验证域名是否在白名单中 用于测试新域名是否可以添加
   *
   * @param origin 待验证的域名
   * @return 验证结果
   */
  @PostMapping("/verify")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Map<String, Object>> verifyDomain(String origin) {
    boolean allowed = domainWhitelistService.isDomainAllowed(origin);

    Map<String, Object> response = new HashMap<>();
    response.put("origin", origin);
    response.put("allowed", allowed);
    response.put("message", allowed ? "域名在白名单中" : "域名不在白名单中");

    return ResultUtils.success(response);
  }

  /**
   * 获取 CORS 配置帮助文档
   *
   * @return 配置帮助
   */
  @GetMapping("/help")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Map<String, Object>> getHelp() {
    Map<String, Object> help = new HashMap<>();

    Map<String, String> configExample = new HashMap<>();
    configExample.put(
        "application.yml",
        "# CORS 配置\n"
            + "cors:\n"
            + "  allowed-origins:\n"
            + "    - http://localhost:3000\n"
            + "    - https://csoj.school.edu.cn\n"
            + "    - https://*.edu.cn  # 支持通配符\n"
            + "  allowed-methods:\n"
            + "    - GET\n"
            + "    - POST\n"
            + "    - PUT\n"
            + "    - DELETE\n"
            + "    - OPTIONS\n"
            + "  allow-credentials: true\n"
            + "  max-age: 3600");

    Map<String, String> envExample = new HashMap<>();
    envExample.put(
        "环境变量",
        "# Linux/Mac\n"
            + "export CORS_ALLOWED_ORIGINS=https://csoj.school.edu.cn,https://*.edu.cn\n\n"
            + "# Windows\n"
            + "set CORS_ALLOWED_ORIGINS=https://csoj.school.edu.cn,https://*.edu.cn");

    help.put("configExample", configExample);
    help.put("envExample", envExample);
    help.put(
        "securityTips",
        Arrays.asList(
            "1. 生产环境不要使用通配符 *",
            "2. 生产环境不要包含 localhost",
            "3. 使用 HTTPS 协议而非 HTTP",
            "4. allowCredentials=true 时避免使用通配符域名模式",
            "5. 定期审查白名单，移除不再使用的域名"));

    return ResultUtils.success(help);
  }
}
