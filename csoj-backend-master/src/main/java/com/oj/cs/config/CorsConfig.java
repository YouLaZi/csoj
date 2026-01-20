package com.oj.cs.config;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.oj.cs.config.properties.CorsProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局跨域配置（安全加固版）
 *
 * <p>安全说明： 1. 不再使用 allowedOriginPatterns("*") 2. 从配置文件读取允许的域名列表 3. 生产环境必须在 application-prod.yml
 * 或环境变量中配置具体域名 4. 支持域名通配符，如 https://*.edu.cn
 */
@Slf4j
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Resource private CorsProperties corsProperties;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    // 获取配置的允许域名列表
    List<String> allowedOrigins = corsProperties.getAllowedOrigins();
    List<String> allowedMethods = corsProperties.getAllowedMethods();
    List<String> exposedHeaders = corsProperties.getExposedHeaders();

    // 日志记录配置的域名（不记录敏感信息）
    log.info("CORS 配置加载: 允许 {} 个域名, {} 种方法", allowedOrigins.size(), allowedMethods.size());

    // 开发环境警告
    if (allowedOrigins.contains("*")
        || allowedOrigins.stream().anyMatch(o -> o.contains("localhost"))) {
      log.warn("⚠️ CORS 配置包含不安全域名（localhost 或 *），请确保仅用于开发环境！");
    }

    registry
        .addMapping("/**")
        // 允许的域名（从配置读取，不使用通配符 *）
        .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
        // 允许的 HTTP 方法
        .allowedMethods(allowedMethods.toArray(new String[0]))
        // 允许的请求头
        .allowedHeaders(corsProperties.getAllowedHeaders())
        // 是否允许发送 Cookie
        .allowCredentials(corsProperties.getAllowCredentials())
        // 预检请求缓存时间（秒）
        .maxAge(corsProperties.getMaxAge())
        // 暴露的响应头
        .exposedHeaders(exposedHeaders.toArray(new String[0]));
  }
}
