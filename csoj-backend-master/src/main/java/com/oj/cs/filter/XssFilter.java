package com.oj.cs.filter;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** XSS 过滤器 防止跨站脚本攻击，添加 CSP 头部 */
@Slf4j
@Component
@WebFilter(urlPatterns = "/*", filterName = "xssFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

  /** CSP 白名单域名（根据实际情况配置） */
  private static final String CSP_DEFAULT_SRC = "'self'";

  private static final String CSP_SCRIPT_SRC = "'self' 'unsafe-inline' 'unsafe-eval'";
  private static final String CSP_STYLE_SRC = "'self' 'unsafe-inline'";
  private static final String CSP_IMG_SRC = "'self' data: https:";
  private static final String CSP_FONT_SRC = "'self' data:";
  private static final String CSP_CONNECT_SRC = "'self'";
  private static final String CSP_FRAME_SRC = "'none'";
  private static final String CSP_OBJECT_SRC = "'none'";
  private static final String CSP_BASE_URI = "'self'";
  private static final String CSP_FORM_ACTION = "'self'";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.info("XSS 过滤器初始化完成");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // 添加 CSP 头部
    addCspHeaders(httpResponse);

    // 添加其他安全头部
    addSecurityHeaders(httpResponse);

    // 包装请求以过滤 XSS
    XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpRequest);

    try {
      chain.doFilter(xssRequest, response);
    } catch (Exception e) {
      log.error("XSS 过滤器处理异常", e);
      throw e;
    }
  }

  /** 添加 CSP (Content Security Policy) 头部 */
  private void addCspHeaders(HttpServletResponse response) {
    StringBuilder cspBuilder = new StringBuilder();
    cspBuilder.append("default-src ").append(CSP_DEFAULT_SRC).append("; ");
    cspBuilder.append("script-src ").append(CSP_SCRIPT_SRC).append("; ");
    cspBuilder.append("style-src ").append(CSP_STYLE_SRC).append("; ");
    cspBuilder.append("img-src ").append(CSP_IMG_SRC).append("; ");
    cspBuilder.append("font-src ").append(CSP_FONT_SRC).append("; ");
    cspBuilder.append("connect-src ").append(CSP_CONNECT_SRC).append("; ");
    cspBuilder.append("frame-src ").append(CSP_FRAME_SRC).append("; ");
    cspBuilder.append("object-src ").append(CSP_OBJECT_SRC).append("; ");
    cspBuilder.append("base-uri ").append(CSP_BASE_URI).append("; ");
    cspBuilder.append("form-action ").append(CSP_FORM_ACTION).append("; ");
    cspBuilder.append("block-all-mixed-content");

    response.setHeader("Content-Security-Policy", cspBuilder.toString());
    // 兼容旧版浏览器
    response.setHeader("X-Content-Security-Policy", cspBuilder.toString());
    response.setHeader("X-WebKit-CSP", cspBuilder.toString());
  }

  /** 添加其他安全相关头部 */
  private void addSecurityHeaders(HttpServletResponse response) {
    // 防止点击劫持
    response.setHeader("X-Frame-Options", "DENY");

    // 防止 MIME 类型嗅探
    response.setHeader("X-Content-Type-Options", "nosniff");

    // 启用浏览器 XSS 过滤器
    response.setHeader("X-XSS-Protection", "1; mode=block");

    // Referrer 策略
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

    // 权限策略（替代 Feature-Policy）
    response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
  }

  @Override
  public void destroy() {
    log.info("XSS 过滤器已销毁");
  }
}
