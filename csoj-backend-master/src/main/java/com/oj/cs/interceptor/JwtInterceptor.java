package com.oj.cs.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.UserService;
import com.oj.cs.utils.JwtUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * JWT 认证拦截器 验证请求头中的 JWT Token
 *
 * @author CSOJ
 * @since 2026-01-17
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

  @Resource private UserService userService;

  /**
   * 在处理请求前验证 Token
   *
   * @param request 请求
   * @param response 响应
   * @param handler 处理器
   * @return true 表示继续执行，false 表示中断
   * @throws Exception 异常
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // 从请求头获取 Token
    String authHeader = request.getHeader("Authorization");
    String token = JwtUtil.extractToken(authHeader);

    if (token == null) {
      log.warn("请求缺少 Token: {}", request.getRequestURI());
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请先登录");
    }

    try {
      // 验证 Token
      if (!JwtUtil.validateToken(token)) {
        log.warn("Token 无效或已过期: {}", request.getRequestURI());
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已过期，请重新登录");
      }

      // 检查 Token 类型
      if (!JwtUtil.isAccessToken(token)) {
        log.warn("Token 类型错误（使用了 Refresh Token）: {}", request.getRequestURI());
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 类型错误");
      }

      // 获取用户 ID 并设置到请求属性中
      Long userId = JwtUtil.getUserIdFromToken(token);
      if (userId == null) {
        log.warn("Token 中无用户 ID: {}", request.getRequestURI());
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token 无效");
      }

      // 查询用户信息
      User user = userService.getById(userId);
      if (user == null) {
        log.warn("用户不存在: userId={}", userId);
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
      }

      // 将用户信息设置到请求属性中，供后续使用
      request.setAttribute("currentUser", user);
      request.setAttribute("currentUserId", userId);

      log.debug("JWT 认证成功: userId={}, uri={}", userId, request.getRequestURI());
      return true;

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("Token 验证异常: {}", request.getRequestURI(), e);
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "认证失败，请重新登录");
    }
  }
}
