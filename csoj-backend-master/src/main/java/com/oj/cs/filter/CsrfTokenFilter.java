package com.oj.cs.filter;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;

/** CSRF Token 过滤器 为每个会话生成 CSRF Token */
@Slf4j
@Component
@WebFilter(urlPatterns = "/*", filterName = "csrfTokenFilter")
public class CsrfTokenFilter implements Filter {

  private static final String CSRF_TOKEN_ATTR = "csrf_token";
  private static final String CSRF_HEADER_NAME = "X-CSRF-Token";
  private static final String CSRF_TOKEN_NAME = "X-CSRF-Token";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.info("CSRF Token 过滤器初始化完成");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // 跳过 GET、HEAD、OPTIONS 请求（只修改状态的需要 CSRF 保护）
    String method = httpRequest.getMethod();
    if ("GET".equalsIgnoreCase(method)
        || "HEAD".equalsIgnoreCase(method)
        || "OPTIONS".equalsIgnoreCase(method)) {
      chain.doFilter(request, response);
      return;
    }

    // 获取会话中的 CSRF Token
    String sessionToken = (String) httpRequest.getSession().getAttribute(CSRF_TOKEN_ATTR);

    // 如果会话中没有 Token，生成新的
    if (sessionToken == null) {
      sessionToken = generateCsrfToken();
      httpRequest.getSession().setAttribute(CSRF_TOKEN_ATTR, sessionToken);
    }

    // 将 Token 添加到响应头
    httpResponse.setHeader(CSRF_HEADER_NAME, sessionToken);

    // 验证请求头中的 Token
    String requestToken = httpRequest.getHeader(CSRF_TOKEN_NAME);
    if (requestToken == null) {
      requestToken = httpRequest.getParameter(CSRF_TOKEN_ATTR);
    }

    if (!sessionToken.equals(requestToken)) {
      log.warn("CSRF Token 验证失败 - Session: {}, Request: {}", sessionToken, requestToken);
      httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      httpResponse.setContentType("application/json;charset=UTF-8");
      httpResponse
          .getWriter()
          .write("{\"code\":403,\"message\":\"CSRF Token 验证失败\",\"data\":null}");
      return;
    }

    chain.doFilter(request, response);
  }

  /** 生成 CSRF Token */
  private String generateCsrfToken() {
    return SecureUtil.md5(
        String.valueOf(System.currentTimeMillis()) + SecureUtil.md5(String.valueOf(Math.random())));
  }

  @Override
  public void destroy() {
    log.info("CSRF Token 过滤器已销毁");
  }
}
