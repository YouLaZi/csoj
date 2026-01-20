package com.oj.cs.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.google.common.util.concurrent.RateLimiter;
import com.oj.cs.annotation.RateLimit;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * 限流拦截器 基于 Guava RateLimiter 实现接口限流
 *
 * @author CSOJ
 * @since 2026-01-17
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

  /** 限流器缓存 key: 限流 key, value: RateLimiter */
  private final ConcurrentHashMap<String, RateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    HandlerMethod handlerMethod = (HandlerMethod) handler;
    Method method = handlerMethod.getMethod();

    // 检查是否有 RateLimit 注解
    RateLimit rateLimit = method.getAnnotation(RateLimit.class);
    if (rateLimit == null) {
      return true;
    }

    // 获取限流 key
    String limitKey = getLimitKey(request, rateLimit);

    // 获取或创建 RateLimiter
    RateLimiter rateLimiter =
        rateLimiterCache.computeIfAbsent(
            limitKey, k -> RateLimiter.create(rateLimit.permitsPerSecond()));

    // 尝试获取许可
    if (!rateLimiter.tryAcquire()) {
      log.warn("请求被限流: key={}, uri={}", limitKey, request.getRequestURI());
      throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "请求过于频繁，请稍后再试");
    }

    return true;
  }

  /**
   * 获取限流 key
   *
   * @param request 请求
   * @param rateLimit 限流注解
   * @return 限流 key
   */
  private String getLimitKey(HttpServletRequest request, RateLimit rateLimit) {
    StringBuilder keyBuilder = new StringBuilder(rateLimit.key());

    switch (rateLimit.mode()) {
      case USER:
        // 基于用户 ID 限流
        Object userId = request.getAttribute("currentUserId");
        if (userId != null) {
          keyBuilder.append(":user:").append(userId);
        } else {
          // 未登录用户使用 IP
          keyBuilder.append(":ip:").append(getClientIp(request));
        }
        break;

      case IP:
        // 基于 IP 限流
        keyBuilder.append(":ip:").append(getClientIp(request));
        break;

      case GLOBAL:
      default:
        // 全局限流，不添加额外标识
        break;
    }

    return keyBuilder.toString();
  }

  /**
   * 获取客户端 IP
   *
   * @param request 请求
   * @return IP 地址
   */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 处理多个 IP 的情况（X-Forwarded-For 可能包含多个 IP）
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}
