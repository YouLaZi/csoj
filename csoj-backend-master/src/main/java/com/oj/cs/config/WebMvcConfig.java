package com.oj.cs.config;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.oj.cs.interceptor.JwtInterceptor;
import com.oj.cs.interceptor.RateLimitInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Web MVC 配置 注册拦截器
 *
 * @author CSOJ
 * @since 2026-01-17
 */
@Slf4j
@Component
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Resource private JwtInterceptor jwtInterceptor;

  @Resource private RateLimitInterceptor rateLimitInterceptor;

  /**
   * 注册拦截器
   *
   * @param registry 拦截器注册器
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 注册限流拦截器（优先级最高，最先执行）
    registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**").order(1);

    // 注册 JWT 拦截器
    registry
        .addInterceptor(jwtInterceptor)
        .addPathPatterns("/api/**") // 拦截所有 API 请求
        .excludePathPatterns(getExcludePathPatterns()) // 排除不需要认证的路径
        .order(2);

    log.info("拦截器已注册: 限流、JWT");
  }

  /**
   * 获取不需要认证的路径列表
   *
   * @return 路径列表
   */
  private String[] getExcludePathPatterns() {
    return new String[] {
      // 登录注册相关
      "/api/user/login",
      "/api/user/login/wx_open",
      "/api/user/register",
      "/api/user/forgot-password",
      "/api/user/reset-password",

      // JWT 认证相关
      "/api/auth/**",

      // 公开访问接口
      "/api/question/list/page/vo",
      "/api/question/get/vo",
      "/api/contest/list/page/vo",
      "/api/contest/get/vo",

      // WebSocket
      "/api/ws/**",

      // 静态资源
      "/api/static/**",
      "/api/files/**",

      // 健康检查
      "/api/actuator/health",
      "/api/actuator/info",

      // API 文档
      "/api/doc.html",
      "/api/swagger-resources/**",
      "/api/v2/api-docs",
      "/api/webjars/**",

      // CORS 预检请求
      "/api/**/options"
    };
  }
}
